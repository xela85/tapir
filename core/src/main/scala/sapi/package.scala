import sapi.TypeMapper.{RequiredTextTypeMapper, TextTypeMapper}
import shapeless.{::, HList, HNil}
import shapeless.ops.hlist.Prepend

import scala.annotation.implicitNotFound

package object sapi {
  /*
    Goals:
    - user-friendly types (also in idea); as simple as possible to generate the client, server & docs
    - Swagger-first
    - reasonably type-safe: only as much as needed to gen a server/client/docs, no more
    - programmer friendly (ctrl-space)
   */

  /*
  Akka http directives:
  - authenticate basic/oauth, authorize (fn)
  - cache responses
  - complete (with)
  - decompress request with
  - add/remove cookie
  - extract headers
  - extract body: entity, form field; save to file
  - method matchers
  - complete with file/directory
  - transform request or response (fn)
  - extract parameters
  - match path (extract suffix, ignore trailing slash)
  - redirects
   */

  // define model using case classes
  // capture path components and their mapping to parameters
  // capture query, body, cookie, header parameters w/ mappings
  // read a yaml to get the model / auto-generate the model from a yaml ?
  //   -> only generation possible, due to type-safety
  //   -> the scala model is richer, as it has all the types + case classes
  // server: generate an http4s/akka endpoint matcher
  // client: generate an sttp request definition

  // separate logic from endpoint definition & documentation

  // provide as much or as little detail as needed: optional query param/endpoint desc, samples
  // reasonably type-safe

  // https://github.com/felixbr/swagger-blocks-scala
  // https://typelevel.org/blog/2018/06/15/typedapi.html (https://github.com/pheymann/typedapi)
  // http://fintrospect.io/defining-routes
  // https://github.com/http4s/rho
  // https://github.com/TinkoffCreditSystems/typed-schema

  // what to capture: path, query parameters, body, headers, default response body, error response body

  // streaming?

  // type: string, format: base64, binary, email, ... - use tagged string types ?
  // type: object                                     - implicit EndpointInputType values
  // form fields, multipart uploads, ...

  // extend the path for an endpoint?
  //
  // types, that you are not afraid to write down
  // Human comprehensible types

  //

  type Id[X] = X
  type Empty[X] = None.type

  @implicitNotFound("???")
  type IsId[U[_]] = U[Unit] =:= Id[Unit]

  sealed trait EndpointInput[I <: HList] {
    def and[J <: HList, IJ <: HList](other: EndpointInput[J])(implicit ts: Prepend.Aux[I, J, IJ]): EndpointInput[IJ]
    def /[J <: HList, IJ <: HList](other: EndpointInput[J])(implicit ts: Prepend.Aux[I, J, IJ]): EndpointInput[IJ] = and(other)
  }

  object EndpointInput {
    sealed trait Single[I <: HList] extends EndpointInput[I] {
      def and[J <: HList, IJ <: HList](other: EndpointInput[J])(implicit ts: Prepend.Aux[I, J, IJ]): EndpointInput[IJ] =
        other match {
          case s: Single[_]     => EndpointInput.Multiple(Vector(this, s))
          case Multiple(inputs) => EndpointInput.Multiple(this +: inputs)
        }
    }

    case class PathSegment(s: String) extends Single[HNil]

    case class PathCapture[T](name: String, m: RequiredTextTypeMapper[T], description: Option[String], example: Option[T])
        extends Single[T :: HNil] {
      def description(d: String): EndpointInput.PathCapture[T] = copy(description = Some(d))
      def example(t: T): EndpointInput.PathCapture[T] = copy(example = Some(t))
    }

    case class Query[T](name: String, m: TextTypeMapper[T], description: Option[String], example: Option[T]) extends Single[T :: HNil] {
      def description(d: String): EndpointInput.Query[T] = copy(description = Some(d))
      def example(t: T): EndpointInput.Query[T] = copy(example = Some(t))
    }

    case class Multiple[I <: HList](inputs: Vector[Single[_]]) extends EndpointInput[I] {
      override def and[J <: HList, IJ <: HList](other: EndpointInput[J])(implicit ts: Prepend.Aux[I, J, IJ]): EndpointInput.Multiple[IJ] =
        other match {
          case s: Single[_] => EndpointInput.Multiple(inputs :+ s)
          case Multiple(m)  => EndpointInput.Multiple(inputs ++ m)
        }
    }
  }

  def pathCapture[T: RequiredTextTypeMapper](name: String): EndpointInput[T :: HNil] =
    EndpointInput.PathCapture(name, implicitly[RequiredTextTypeMapper[T]], None, None)
  implicit def stringToPath(s: String): EndpointInput[HNil] = EndpointInput.PathSegment(s)

  def query[T: TextTypeMapper](name: String): EndpointInput.Query[T] = EndpointInput.Query(name, implicitly[TextTypeMapper[T]], None, None)

  case class Endpoint[U[_], I <: HList, O](name: Option[String],
                                           method: U[Method],
                                           input: EndpointInput.Multiple[I],
                                           output: TypeMapper[O, Nothing],
                                           summary: Option[String],
                                           description: Option[String],
                                           okResponseDescription: Option[String],
                                           errorResponseDescription: Option[String],
                                           tags: List[String]) {
    def name(s: String): Endpoint[U, I, O] = this.copy(name = Some(s))

    def get(): Endpoint[Id, I, O] = this.copy[Id, I, O](method = Method.GET)
    def post(): Endpoint[Id, I, O] = this.copy[Id, I, O](method = Method.POST)

    def in[J <: HList, IJ <: HList](i: EndpointInput[J])(implicit ts: Prepend.Aux[I, J, IJ]): Endpoint[U, IJ, O] =
      this.copy[U, IJ, O](input = input.and(i))

    def out[T, M <: MediaType](implicit tm: TypeMapper[T, M]): Endpoint[U, I, T] = copy[U, I, T](output = tm)

    def summary(s: String): Endpoint[U, I, O] = copy(summary = Some(s))
    def description(d: String): Endpoint[U, I, O] = copy(description = Some(d))
    def okResponseDescription(d: String): Endpoint[U, I, O] = copy(okResponseDescription = Some(d))
    def errorResponseDescription(d: String): Endpoint[U, I, O] = copy(errorResponseDescription = Some(d))
    def tags(ts: List[String]): Endpoint[U, I, O] = copy(tags = ts)
    def tag(t: String): Endpoint[U, I, O] = copy(tags = t :: tags)
  }

  case class InvalidOutput(reason: DecodeResult[Nothing], cause: Option[Throwable]) extends Exception(cause.orNull)
//  case class InvalidInput(input: EndpointInput.Single[_], reason: TypeMapper.Result[Nothing], cause: Option[Throwable])
//      extends Exception(cause.orNull)

  val endpoint: Endpoint[Empty, HNil, Unit] =
    Endpoint[Empty, HNil, Unit](None, None, EndpointInput.Multiple(Vector.empty), implicitly, None, None, None, None, Nil)
}