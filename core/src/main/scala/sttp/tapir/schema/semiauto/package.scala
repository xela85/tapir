package sttp.tapir.schema

import magnolia.{Magnolia, ReadOnlyCaseClass, SealedTrait}
import sttp.tapir.Schema
import sttp.tapir.generic.Configuration
import sttp.tapir.generic.internal.SchemaMagnoliaDerivation

package object semiauto {
  type Typeclass[T] = Schema[T]

  def combine[T](ctx: ReadOnlyCaseClass[Schema, T])(implicit genericDerivationConfig: Configuration): Schema[T] =
    SchemaMagnoliaDerivation.combine(ctx)

  def dispatch[T](ctx: SealedTrait[Schema, T])(implicit genericDerivationConfig: Configuration): Schema[T] =
    SchemaMagnoliaDerivation.dispatch(ctx)

  def deriveSchema[T]: Typeclass[T] = macro Magnolia.gen[T]
}
