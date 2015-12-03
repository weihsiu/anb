package anb

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.language.implicitConversions

object Callbacks extends App {

  import Orders._

  object CallbackService {
    import Blockings.BlockingService
    def getCustomer(id: String, callback: Customer => Unit): Unit = callback(BlockingService.getCustomer(id))
    def getSupplier(id: String, callback: Supplier => Unit): Unit = callback(BlockingService.getSupplier(id))
    def getProduct(id: String, callback: Product => Unit): Unit = callback(BlockingService.getProduct(id))
    def getOrder(id: String, callback: Order => Unit): Unit = callback(BlockingService.getOrder(id))
  }

  object AsyncCallbackService {
    import Blockings.BlockingService
    val ec = ExecutionContext.global
    implicit def function0ToRunnable(body: => Unit): Runnable = new Runnable { def run = body }
    def getCustomer(id: String, callback: Customer => Unit): Unit = ec.execute(callback(BlockingService.getCustomer(id)))
    def getSupplier(id: String, callback: Supplier => Unit): Unit = ec.execute(callback(BlockingService.getSupplier(id)))
    def getProduct(id: String, callback: Product => Unit): Unit = ec.execute(callback(BlockingService.getProduct(id)))
    def getOrder(id: String, callback: Order => Unit): Unit = ec.execute(callback(BlockingService.getOrder(id)))
  }

  object CallbackClient {
    import CallbackService._
//    import AsyncCallbackService._
    def getFullOrder(id: String, callback: FullOrder => Unit): Unit = {
      getOrder(id, order => {
        getCustomer(order.customerId, customer => {
          var fullProducts = List.empty[FullProduct] // ouch!! mutable var!!!
          order.productIds foreach { pid =>
            getProduct(pid, product => {
              getSupplier(product.supplierId, supplier => {
                val fullProduct = FullProduct(product.id, product.name, supplier)
                fullProducts :+= fullProduct
                if (fullProducts.length == order.productIds.length)
                  callback(FullOrder(order.id, customer, fullProducts))
              })
            })
          }
        })
      })
    }
  }

  val start = System.currentTimeMillis
  CallbackClient.getFullOrder("100", { fullOrder =>
    println(fullOrder)
    println(s"elapsed time ${System.currentTimeMillis - start}ms")
  })

  StdIn.readLine
}
