package com.intenthq.properties

import uscala.result.Result
import uscala.result.Result.Ok

object stringconversions {
  implicit def throwableToString[T](r: Result[Throwable, T]): Result[String, T] = r.mapFail(_.getMessage)

  implicit val parseLong: String => Result[String, Long] = input => Result.attempt(input.toLong)

  implicit val parseInt: String => Result[String, Int] = input => Result.attempt(input.toInt)

  implicit val parseDouble: String => Result[String, Double] = input => Result.attempt(input.toDouble)

  implicit val parseBoolean: String => Result[String, Boolean] = input => Result.attempt(input.toBoolean)

  implicit val parseString: String => Result[String, String] = Ok(_)
}
