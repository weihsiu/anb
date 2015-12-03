package anb

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Futures extends App {

  import Orders._

  object FutureService {
    import Blockings.BlockingService
    def getCustomer(id: String): Future[Customer] = Future(blocking(BlockingService.getCustomer(id)))
    def getSupplier(id: String): Future[Supplier] = Future(blocking(BlockingService.getSupplier(id)))
    def getProduct(id: String): Future[Product] = Future(blocking(BlockingService.getProduct(id)))
    def getOrder(id: String): Future[Order] = Future(blocking(BlockingService.getOrder(id)))
  }

  object FutureClient {
    import FutureService._
    def getFullOrder(id: String): Future[FullOrder] = {
      for {
        order <- getOrder(id)
        customer <- getCustomer(order.customerId)
        products <- Future.sequence(order.productIds.map(getProduct(_)))
        suppliers <- Future.sequence(products.map(p => getSupplier(p.id)))
        fullProducts = products.zip(suppliers).map { case (p, s) => FullProduct(p.id, p.name, s) }
      } yield FullOrder(order.id, customer, fullProducts)
    }
  }

  val start = System.currentTimeMillis
  println(Await.result(FutureClient.getFullOrder("100"), 5 seconds))
  println(s"elapsed time ${System.currentTimeMillis - start}ms")
}
