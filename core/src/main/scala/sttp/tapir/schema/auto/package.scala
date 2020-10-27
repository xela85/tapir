package sttp.tapir.schema

import magnolia.{ReadOnlyCaseClass, SealedTrait}
import sttp.tapir.generic.{Configuration, Derived}
import sttp.tapir.generic.internal.{MagnoliaDerivedMacro, SchemaMagnoliaDerivation}
import sttp.tapir.{LowPrioritySchema, Schema}

package object auto extends LowPrioritySchema {
  type Typeclass[T] = Schema[T]

  def combine[T](ctx: ReadOnlyCaseClass[Schema, T])(implicit genericDerivationConfig: Configuration): Schema[T] =
    SchemaMagnoliaDerivation.combine(ctx)

  def dispatch[T](ctx: SealedTrait[Schema, T])(implicit genericDerivationConfig: Configuration): Schema[T] = dispatch(ctx)

  implicit def schemaForCaseClass[T]: Derived[Schema[T]] = macro MagnoliaDerivedMacro.derivedGen[T]

}
