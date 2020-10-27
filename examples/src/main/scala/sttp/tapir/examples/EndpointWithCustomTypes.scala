package sttp.tapir.examples

import io.circe.{Decoder, Encoder}
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.schema.auto._
import sttp.tapir.validator.auto._
import io.circe.generic.auto._

object EndpointWithCustomTypes {
  // An over-complicated, example custom type
  trait MyId {
    def id: String
  }
  class MyIdImpl(val id: String) extends MyId

  // Custom type as a path or query parameter: encoding and decoding is fully handled by tapir. We need to provide
  // a custom implicit Codec
  implicit val myIdCodec: Codec[String, MyId, CodecFormat.TextPlain] =
    Codec.string.map[MyId](s => new MyIdImpl(s))(myId => myId.id)
  val endpointWithMyId: Endpoint[MyId, Unit, Unit, Nothing] = endpoint.in("find" / path[MyId])

  // Custom type mapped to json: encoding and decoding is handled by circe. The Codec is automatically derived from a
  // circe Encoder and Decoder. We also need the schema (through the SchemaFor implicit) for documentation.
  case class Person(id: MyId, name: String)

  implicit val myIdSchema: Schema[MyId] = Schema(SchemaType.SString)
  // custom circe encoders and decoders need to be in-scope as well
  implicit val myIdEncoder: Encoder[MyId] = Encoder.encodeString.contramap(_.id)
  implicit val myIdDecoder: Decoder[MyId] = Decoder.decodeString.map(s => new MyIdImpl(s))
  val endpointWithPerson: Endpoint[Unit, Unit, Person, Nothing] = endpoint.out(jsonBody[Person])
}
