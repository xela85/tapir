package sttp.tapir.json.upickle

import upickle.default._
import org.scalatest.Assertion

import sttp.tapir.Codec.JsonCodec
import sttp.tapir._
import sttp.tapir.DecodeResult._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

object TapirJsonuPickleCodec extends TapirJsonuPickle

class TapirJsonuPickleTests extends AnyFlatSpec with TapirJsonuPickleTestExtensions with Matchers {
  case class Customer(name: String, yearOfBirth: Int, lastPurchase: Option[Long])

  object Customer {
    implicit val rw: ReadWriter[Customer] = macroRW
  }

  val customerDecoder: JsonCodec[Customer] = TapirJsonuPickleCodec.readWriterCodec[Customer]

  // Helper to test encoding then decoding an object is the same as the original
  def testEncodeDecode[T: ReadWriter: Schema: Validator](original: T): Assertion = {
    val codec = TapirJsonuPickleCodec.readWriterCodec[T]

    val encoded = codec.encode(original)
    codec.decode(encoded) match {
      case Value(d) =>
        d shouldBe original
      case f: DecodeResult.Failure =>
        fail(f.toString)
    }
  }

  it should "encode and decode Scala case class with non-empty Option elements" in {
    val customer = Customer("Alita", 1985, Some(1566150331L))
    testEncodeDecode(customer)
  }

  it should "encode and decode Scala case class with empty Option elements" in {
    val customer = Customer("Alita", 1985, None)
    testEncodeDecode(customer)
  }

  it should "encode and decode String type" in {
    testEncodeDecode("Hello, World!")
  }

  it should "encode and decode Long type" in {
    testEncodeDecode(1566150331L)
  }

}
