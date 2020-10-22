package sttp.tapir.generic

import sttp.tapir.Schema
import sttp.tapir.generic.internal.{MagnoliaDerivedMacro, SchemaMagnoliaDerivation}

import scala.annotation.implicitNotFound

package object semiauto extends SchemaMagnoliaDerivation {
  def deriveSchema[T](implicit derived: Derived[Schema[T]]): Schema[T] = derived.value
}
