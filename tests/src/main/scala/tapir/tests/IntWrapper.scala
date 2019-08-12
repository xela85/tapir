package tapir.tests

final class IntWrapper(val value: Int) extends AnyVal
object IntWrapper {
  implicit val numeric: Numeric[IntWrapper] = new Numeric[IntWrapper] {
    override def plus(x: IntWrapper, y: IntWrapper): IntWrapper = IntWrapper(x.value + y.value)
    override def minus(x: IntWrapper, y: IntWrapper): IntWrapper = IntWrapper(x.value - y.value)
    override def times(x: IntWrapper, y: IntWrapper): IntWrapper = IntWrapper(x.value * y.value)
    override def negate(x: IntWrapper): IntWrapper = IntWrapper(-x.value)
    override def fromInt(x: Int): IntWrapper = IntWrapper(x)
    override def toInt(x: IntWrapper): Int = x.value
    override def toLong(x: IntWrapper): Long = x.value.toLong
    override def toFloat(x: IntWrapper): Float = x.value.toFloat
    override def toDouble(x: IntWrapper): Double = x.value.toDouble
    override def compare(x: IntWrapper, y: IntWrapper): Int = x.value.compare(y.value)
  }
  def apply(int: Int) = new IntWrapper(int)
}
