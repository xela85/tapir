package sttp.tapir.validator

import com.github.ghik.silencer.silent
import magnolia.{Magnolia, ReadOnlyCaseClass, SealedTrait}
import sttp.tapir.Validator
import sttp.tapir.generic.Configuration
import sttp.tapir.generic.internal.ValidatorMagnoliaDerivation

package object auto {
  type Typeclass[T] = Validator[T]

  def combine[T](ctx: ReadOnlyCaseClass[Validator, T])(implicit genericDerivationConfig: Configuration): Validator[T] =
    ValidatorMagnoliaDerivation.combine(ctx)

  @silent("never used")
  def dispatch[T](ctx: SealedTrait[Validator, T]): Validator[T] = ValidatorMagnoliaDerivation.dispatch(ctx)

  def fallback[T]: Validator[T] = Validator.pass

  implicit def validatorForCaseClass[T]: Typeclass[T] = macro Magnolia.gen[T]
}
