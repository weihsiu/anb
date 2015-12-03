val i = 123
//i = 456

var s = "hello"
s = "world"

def add(x: Int, y: Int): Int = x + y
//val add: (Int, Int) => Int = (x, y) => x + y

case class Person(name: String, age: Int)

val p1 = Person("walter", 18)

case object PersonService {
  def getPerson(name: String): Person = Person(name, name.length * 3)
}

val p2 = PersonService.getPerson("Adrian")







class User(_name: String, _age: Int) {
  val name: String = _name
  val age: Int = _age
  override def toString: String = s"User($name, $age)"
  override def hashCode: Int = ???
  override def equals(other: Any): Boolean = ???
}

case class User2(name: String, age: Int)

val user = User2("walter", 18)

def showUser(user: User2): String = user match {
  case User2(n, a) => s"user $n is $a years old"
}

val users = List(User2("walter", 18), User2("adrian", 21), User2("brian", 32))

val showUsers = users.map(showUser)

val moreUsers = users.flatMap(user => List.fill(2)(user))

val showMoreUsers = moreUsers.map(showUser)

trait Appendable[A] {
  def append(x: A, y: A): A
}

object Appendable {
  implicit val intAppendable = new Appendable[Int] {
    def append(x: Int, y: Int) = x + y
  }
  implicit val stringAppendable = new Appendable[String] {
    def append(x: String, y: String) = x + y
  }
  def append[A : Appendable](x: A, y: A): A = implicitly[Appendable[A]].append(x, y)
}

import Appendable._

val s1 = intAppendable.append(1, 2)
val s2 = stringAppendable.append("hello ", "world")

val s3 = append(3, 4)
val s4 = append("goodbye ", "world")