package anb

import scala.concurrent.duration._
import scala.language.postfixOps

object Blockings extends App {

  import Orders._

  object BlockingService {
    def blockFor[A](d: Duration, x: A): A = {
      Thread.sleep(d.toMillis)
      x
    }
    val duration = 1 second
//    val duration = 1 millisecond
    def getCustomer(id: String): Customer = blockFor(duration, Customer(id, "Walter", "1, Infinite Loop"))
    def getSupplier(id: String): Supplier = blockFor(duration, Supplier(id, "ACME", "A Company that Makes Everything"))
    def getProduct(id: String): Product = blockFor(duration, Product(id, "Perpetual Motion Machine", s"S${id.tail}"))
    def getOrder(id: String): Order = blockFor(duration, Order(id, s"C$id", (1 to 5).map(n => s"P$id$n").toList))
  }

  object BlockingClient {
    import BlockingService._
    def getFullOrder(id: String): FullOrder = {
      val order = getOrder(id)
      val customer = getCustomer(order.customerId)
      val fullProducts = order.productIds map { pid =>
        val product = getProduct(pid)
        val supplier = getSupplier(product.supplierId)
        FullProduct(product.id, product.name, supplier)
      }
      FullOrder(id, customer, fullProducts)
    }
  }

  val start = System.currentTimeMillis
  println(BlockingClient.getFullOrder("100"))
  println(s"elapsed time ${System.currentTimeMillis - start}ms")
}
