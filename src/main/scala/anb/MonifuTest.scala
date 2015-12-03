package anb

import monifu.concurrent.Implicits.globalScheduler
import monifu.reactive._
import scala.io.StdIn

object MonifuTest extends App {

  import Orders._

  object MonifuService {
    import Futures.FutureService
    def getCustomer(id: String): Observable[Customer] = FutureService.getCustomer(id)
    def getSupplier(id: String): Observable[Supplier] = FutureService.getSupplier(id)
    def getProduct(id: String): Observable[Product] = FutureService.getProduct(id)
    def getOrder(id: String): Observable[Order] = FutureService.getOrder(id)
  }

  object MonifuClient {
    import MonifuService._
    def getFullOrder(id: String): Observable[FullOrder] = {
      for {
        order <- getOrder(id)
        customer <- getCustomer(order.customerId)
        fullProducts <- Observable.combineLatestList(
          order.productIds.map(pid =>
            getProduct(pid).flatMap(p =>
              getSupplier(p.supplierId).map(s =>
                FullProduct(p.id, p.name, s)
              )
            )
          ): _*
        )
      } yield FullOrder(order.id, customer, fullProducts.toList)
    }
  }

  val start = System.currentTimeMillis
  MonifuClient.getFullOrder("100").foreach { fullOrder =>
    println(fullOrder)
    println(s"elapsed time ${System.currentTimeMillis - start}ms")
  }

  StdIn.readLine
}
