package anb

import rx._
import rx.ops._

import scala.io.StdIn

object Rxs extends App {

  import Orders._

  object RxService {
    def getCustomer(id: String): Rx[Customer] = ???
    def getSupplier(id: String): Rx[Supplier] = ???
    def getProduct(id: String): Rx[Product] = ???
    def getOrder(id: String): Rx[Order] = ???
  }

  object RxClient {
    import RxService._
    def getFullOrder(id: String): Rx[FullOrder] = {
      var order = getOrder(id)
      var customer = getCustomer(order().customerId)
      var products = order().productIds.map(getProduct(_))
      var fullProducts = products.map(_.map(p => FullProduct(p.id, p.name, getSupplier(p.supplierId)()))).map(_())
      Var(FullOrder(id, customer(), fullProducts))
    }
  }

  val start = System.currentTimeMillis
  RxClient.getFullOrder("100").foreach { fullOrder =>
    println(fullOrder)
    println(s"elapsed time ${System.currentTimeMillis - start}ms")
  }

  StdIn.readLine
}
