package tapir

import scala.util.matching.Regex

sealed trait Constraint[T] { outer =>
  def check(actual: T): Boolean
  def map[TT](f: TT => T): Constraint[TT] = {
    new Constraint[TT] {
      override def check(actual: TT): Boolean = outer.check(f(actual))
    }
  }
}
object Constraint {

  case class Minimum[T: Numeric](value: T) extends Constraint[T] {
    override def check(actual: T): Boolean = implicitly[Numeric[T]].gteq(actual, value)
  }

  case class Maximum[T: Numeric](value: T) extends Constraint[T] {
    override def check(actual: T): Boolean = implicitly[Numeric[T]].lteq(actual, value)
  }

  case class Pattern(value: String) extends Constraint[String] {
    override def check(actual: String): Boolean = actual.matches(value)
  }

  case class MaxLength(value: Int) extends Constraint[String] {
    override def check(actual: String): Boolean = actual.length <= value
  }

  case class MinLength(value: Int) extends Constraint[String] {
    override def check(actual: String): Boolean = actual.length >= value
  }

  case class MinItems(value: Int) extends Constraint[Iterable[_]] {
    override def check(actual: Iterable[_]): Boolean = actual.size >= value
  }

  case class MaxItems(value: Int) extends Constraint[Iterable[_]] {
    override def check(actual: Iterable[_]): Boolean = actual.size <= value
  }

  case class Enum[T](values: List[T]) extends Constraint[T] {
    override def check(actual: T): Boolean = values.contains(actual)
  }

  object Enum {
    def apply[T](v: T*): Enum[T] = Enum(v.toList)
  }

}
