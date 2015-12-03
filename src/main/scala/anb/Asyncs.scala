package anb

import scala.async.Async._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Asyncs extends App {

  import Orders._

  object AsyncClient {
    import Futures.FutureService._
    def getFullOrder(id: String): Future[FullOrder] = async {
      val order = await(getOrder(id))
      val customer = await(getCustomer(order.customerId))
      val fullProducts = Future.sequence(order.productIds map { pid =>
        async {
          val product = await(getProduct(pid))
          val supplier = await(getSupplier(product.supplierId))
          FullProduct(product.id, product.name, supplier)
        }
      })
      FullOrder(id, customer, await(fullProducts))
    }
  }

  val start = System.currentTimeMillis
  println(Await.result(AsyncClient.getFullOrder("100"), 5 seconds))
  println(s"elapsed time ${System.currentTimeMillis - start}ms")
}
