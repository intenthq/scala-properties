package com.intenthq.properties

import uscala.result.Result

trait StringConversion[T] {
  def parse(input: String): Result[String, T]
}

object StringConversion {
  val ParseErrorFormat = "Could not parse value '%s' as %s"

  implicit object ParseString extends StringConversion[String] {
    override def parse(input: String): Result[String, String] = Result.ok(input)
  }

  implicit object ParseInt extends StringConversion[Int] {
    override def parse(input: String): Result[String, Int] =
      Result.attempt(input.toInt).mapFail(_ => ParseErrorFormat.format(input, "int"))
  }

  implicit object ParseLong extends StringConversion[Long] {
    override def parse(input: String): Result[String, Long] =
      Result.attempt(input.toLong).mapFail(_ => ParseErrorFormat.format(input, "long"))
  }

  implicit object ParseDouble extends StringConversion[Double] {
    override def parse(input: String): Result[String, Double] =
      Result.attempt(input.toDouble).mapFail(_ => ParseErrorFormat.format(input, "double"))
  }

  implicit object ParseBoolean extends StringConversion[Boolean] {
    override def parse(input: String): Result[String, Boolean] =
      Result.attempt(input.toBoolean).mapFail(_ => ParseErrorFormat.format(input, "boolean"))
  }
}
