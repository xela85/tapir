package sttp.tapir.generic.internal

import com.github.ghik.silencer.silent
import magnolia.{Magnolia, ReadOnlyCaseClass, SealedTrait}
import sttp.tapir.{FieldName, Validator, encodedName, generic}
import sttp.tapir.generic.Configuration

object ValidatorMagnoliaDerivation {

  def combine[T](ctx: ReadOnlyCaseClass[Validator, T])(implicit genericDerivationConfig: Configuration): Validator[T] = {
    Validator.Product(ctx.parameters.map { p =>
      p.label -> new Validator.ProductField[T] {
        override type FieldType = p.PType
        override def name: FieldName =
          FieldName(p.label, getEncodedName(p.annotations).getOrElse(genericDerivationConfig.toEncodedName(p.label)))
        override def get(t: T): FieldType = p.dereference(t)
        override def validator: Validator[FieldType] = p.typeclass
      }
    }.toMap)
  }

  private def getEncodedName(annotations: Seq[Any]): Option[String] =
    annotations.collectFirst { case ann: encodedName => ann.name }

  @silent("never used")
  def dispatch[T](ctx: SealedTrait[Validator, T]): Validator[T] =
    Validator.Coproduct(new generic.SealedTrait[Validator, T] {
      override def dispatch(t: T): Validator[T] = ctx.dispatch(t) { v => v.typeclass.asInstanceOf[Validator[T]] }

      override def subtypes: Map[String, Validator[Any]] =
        ctx.subtypes.map(st => st.typeName.full -> st.typeclass.asInstanceOf[Validator[scala.Any]]).toMap
    })
}
