package sttp.tapir.server

import com.github.ghik.silencer.silent
import sttp.tapir.Validator
import sttp.tapir.validator.semiauto.deriveValidator
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ServerDefaultsTest extends AnyFlatSpec with Matchers {
  it should "create a validation error message for a nested field" in {
    // given
    @silent("never used")
    implicit val addressNumberValidator: Validator[Int] = Validator.min(1)
    @silent("fallback derivation")
    val personValidator = deriveValidator[Person]

    // when
    val validationErrors = personValidator.validate(Person("John", Address("Lane", 0)))

    // then
    ServerDefaults.ValidationMessages.validationErrorsMessage(
      validationErrors
    ) shouldBe "expected address.number to be greater than or equal to 1, but was 0"
  }

  case class Person(name: String, address: Address)
  case class Address(street: String, number: Int)
}
