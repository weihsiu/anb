package anb

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import anb.Orders.FullOrder
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn
import scala.language.postfixOps

object Actors extends App {

  object OrdersActor {
    def props: Props = Props(new OrdersActor)
    case class GetFullOrder(id: String)
  }

  class OrdersActor extends Actor {
    import Futures.FutureClient._
    import OrdersActor._
    def receive = {
      case GetFullOrder(id) =>
        val replyTo = sender
        getFullOrder(id).foreach(fullOrder => replyTo ! fullOrder)
    }
  }

  import OrdersActor._
  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)

  val ordersActor = system.actorOf(OrdersActor.props)
  val fullOrderF: Future[FullOrder] = (ordersActor ? GetFullOrder("123")).mapTo[FullOrder]
  fullOrderF.foreach(println)

  StdIn.readLine
  system.terminate
}
