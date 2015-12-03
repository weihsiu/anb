package anb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object AkkaStreams extends App {

  implicit val system = ActorSystem("AkkaStreams")
  implicit val materializer = ActorMaterializer()

  val start = System.currentTimeMillis
  Source
      .tick(0 second, 1 second, ())
      .drop(5)
      .zipWith(Source(1 to Int.MaxValue))((_, n) => n.toString)
      .mapAsync(1)(Asyncs.AsyncClient.getFullOrder(_))
      .filter(_.products.length > 3)
      .groupedWithin(Int.MaxValue, 3 seconds)
      .map(_.head.customer.name)
      .runForeach(s => println(s"$s, ${System.currentTimeMillis - start} ms elapsed"))

  StdIn.readLine
  system.terminate
}
