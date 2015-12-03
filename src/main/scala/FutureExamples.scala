import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn
import scala.util.Try

object FutureExamples extends App {

  val strP = Promise[String]

  val strF = strP.future

  strF.foreach(s => println(s"strF completes with $s"))

  val intF = strF.map(s => s.toInt)

  intF.foreach(l => println(s"intF completes with $l"))

//  intF.recover({
//    case _ => -1
//  }).foreach(l => println(s"intF completes with $l"))

  strP.complete(Try("123"))
//  strP.complete(Try("abc"))

  StdIn.readLine
}
