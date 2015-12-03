package anb

import monifu.concurrent.Implicits.globalScheduler
import monifu.reactive._
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object Monifus extends App {
  val start = System.currentTimeMillis
  val subscription = Observable
      .interval(1 second)
      .dropByTimespan(5 seconds)
      .flatMap(n => Asyncs.AsyncClient.getFullOrder(n.toString))
      .filter(_.products.length > 3)
      .sampleRepeated(3 seconds)
      .map(fullOrder => fullOrder.customer.name)
      .doWork(s => println(s"$s, ${System.currentTimeMillis - start} ms elapsed"))
      .subscribe()
  StdIn.readLine
  subscription.cancel
}
