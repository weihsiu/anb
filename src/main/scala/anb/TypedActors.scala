package anb

import akka.actor.{ActorRefFactory, ActorContext, PoisonPill, ActorSystem}
import akka.util.Timeout
import de.knutwalker.akka.typed._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

object TypedActors {

  import Orders._

  implicit val timeout = Timeout(5 seconds)

  def typedActorOf[A: ClassTag](f: ActorContext => A => Unit)(implicit factory: ActorRefFactory): ActorRef[A] =
    ActorOf(PropsFor(new TypedActor.Of[A] { def typedReceive = Total(f(context)) }))

  sealed trait OrderCommand
  case class GetCustomer(id: String)(val replyTo: ActorRef[GotCustomer]) extends OrderCommand
  case class GetProduct(id: String)(val replyTo: ActorRef[GotProduct]) extends OrderCommand
  case class GetSupplier(id: String)(val replyTo: ActorRef[GotSupplier]) extends OrderCommand
  case class GetOrder(id: String)(val replyTo: ActorRef[GotOrder]) extends OrderCommand

  sealed trait OrderResult
  case class GotCustomer(customer: Customer) extends OrderResult
  case class GotProduct(product: Product) extends OrderResult
  case class GotSupplier(supplier: Supplier) extends OrderResult
  case class GotOrder(order: Order) extends OrderResult

  case class OrderActor() extends TypedActor.Of[OrderCommand] {
    import Futures.FutureService._
    import scala.concurrent.ExecutionContext.Implicits.global
    def typedReceive = Total {
      case m @ GetCustomer(id) => getCustomer(id).foreach(m.replyTo ! GotCustomer(_))
      case m @ GetProduct(id) => getProduct(id).foreach(m.replyTo ! GotProduct(_))
      case m @ GetSupplier(id) => getSupplier(id).foreach(m.replyTo ! GotSupplier(_))
      case m @ GetOrder(id) => getOrder(id).foreach(m.replyTo ! GotOrder(_))
    }
  }

  case class GetFullProducts(ids: List[String])(val replyTo: ActorRef[GotFullProducts])
  case class GotFullProducts(fullProducts: List[FullProduct])
  case class GotFullProduct(fullProduct: FullProduct)

  case class FullProductsActor(orderActor: ActorRef[OrderCommand]) extends TypedActor.Of[GetFullProducts | GotFullProduct | GotProduct | GotSupplier] {
    def typedReceive = init
    val init: TypedReceive = Union
      .on[GetFullProducts] { case m @ GetFullProducts(ids) =>
        ids.foreach(orderActor ! GetProduct(_)(typedSelf.only[GotProduct]))
        typedBecome(processing(m.replyTo, ids.length, List.empty))
      }
      .apply
    def processing(replyTo: ActorRef[GotFullProducts], len: Int, fullProducts: List[FullProduct]): TypedReceive =
      if (len == fullProducts.length) {
        replyTo ! GotFullProducts(fullProducts)
        init
      } else Union
        .on[GotProduct] { case GotProduct(product) =>
          orderActor ! GetSupplier(product.supplierId)(typedActorOf[GotSupplier] { context => {
            case GotSupplier(supplier) =>
              typedSelf ! GotFullProduct(FullProduct(product.id, product.name, supplier))
              context.stop(context.self)
          }})
        }
        .on[GotFullProduct] { case GotFullProduct(fullProduct) =>
          typedBecome(processing(replyTo, len, fullProduct :: fullProducts))
        }
        .apply
  }

  case class GetFullOrder(id: String)(val replyTo: ActorRef[GotFullOrder])
  case class GotFullOrder(fullOrder: FullOrder)

  case class FullOrderActor(orderActor: ActorRef[OrderCommand]) extends TypedActor.Of[GetFullOrder | GotOrder | GotCustomer | GotFullProducts] {
    val fullProductsActor = Typed[FullProductsActor].create(orderActor)
    def typedReceive = init
    val init: TypedReceive = Union
      .on[GetFullOrder] { case m @ GetFullOrder(id) =>
        orderActor ! GetOrder(id)(typedSelf.only[GotOrder])
        typedBecome(processing(m.replyTo, None, None, None))
      }
      .apply
    def processing(replyTo: ActorRef[GotFullOrder], order: Option[Order], customer: Option[Customer], fullProducts: Option[List[FullProduct]]): TypedReceive =
      if (order.nonEmpty && customer.nonEmpty && fullProducts.nonEmpty) {
        replyTo ! GotFullOrder(FullOrder(order.get.id, customer.get, fullProducts.get))
        init
      } else Union
        .on[GotOrder] { case GotOrder(order) =>
          orderActor ! GetCustomer(order.customerId)(typedSelf.only[GotCustomer])
          fullProductsActor ! GetFullProducts(order.productIds)(typedSelf.only[GotFullProducts])
          typedBecome(processing(replyTo, Some(order), customer, fullProducts))
        }
        .on[GotCustomer] { case GotCustomer(customer) =>
          typedBecome(processing(replyTo, order, Some(customer), fullProducts))
        }
        .on[GotFullProducts] { case GotFullProducts(fullProducts) =>
          typedBecome(processing(replyTo, order, customer, Some(fullProducts)))
        }
        .apply
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("TypedActors")

    val orderActor = Typed[OrderActor].create()
    val fullOrderActor = Typed[FullOrderActor].create(orderActor)

    val start = System.currentTimeMillis
    println(Await.result(fullOrderActor ? GetFullOrder("100"), 5 seconds))
    println(s"elapsed time ${System.currentTimeMillis - start}ms")

    system.terminate
  }
}
