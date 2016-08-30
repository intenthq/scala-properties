package com.intenthq.properties

import java.util.Properties

import uscala.result.Result
import uscala.result.Result.{Fail, Ok}

trait PropertyReader {
  def readSafe(name: String): Option[String]

  def orError(name: String): Result[String, String] =
    Result.fromOption(readSafe(name), PropertyReader.MissingValueFormat.format(name))

  def required[T](name: String)(implicit conversion: StringConversion[T]): Result[String, T] =
    orError(name).flatMap(conversion.parse)

  def optional[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] =
    readSafe(name).map(conversion.parse).fold[Result[String, Option[T]]](Ok(None))(_.map(Some(_)))
}

object PropertyReader {
  val MissingValueFormat = "'%s' is missing from the configuration"
}

class MapPropertyReader(val props: Map[String, String]) extends PropertyReader {
  override def readSafe(name: String): Option[String] = props.get(name)
}

class SystemPropertyReader(val props: Properties = System.getProperties) extends PropertyReader {
  override def readSafe(name: String): Option[String] = Option(props.getProperty(name)).filterNot(_ == "")
}

class CombinedReader (val readers: Set[PropertyReader]) extends PropertyReader {
  def collectErrors[T](results: Seq[(String, Result[String, T])]): Result[String, T] =
    results.find(_._2.isInstanceOf[Ok[_]]).fold[Result[String, T]](
      Fail(results.filter(_._2.isInstanceOf[Fail[_]]).foldLeft("")((acc, v) =>
        v._2.fold(err => s"$acc${v._1}: $err\n", _ => acc)
      ).trim)
    )(_._2)

  override def readSafe(name: String): Option[String] = readers.flatMap(reader => reader.readSafe(name)).headOption

  override def orError(name: String): Result[String, String] =
    collectErrors(readers.toSeq.map(r => (r.getClass.getSimpleName, r.orError(name))))

  override def optional[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] = {
    val results = readers.toSeq.map(r => (r.getClass.getSimpleName, r.optional(name)))
    val filtered =
      if (results.exists(_._2.toEither.isLeft)) results.filter(_._2.toEither.isLeft) // if there are any errors then make sure there are no results so the user can see the errors
      else if (results.exists(_._2.toOption.flatten.isDefined)) results.filter(_._2.toOption.flatten.isDefined) // if there are any valid results then filter out the `None`s
      else results // otherwise just return the results

    collectErrors(filtered)
  }
}

object CombinedReader {
  def apply(readers: PropertyReader*): CombinedReader = new CombinedReader(readers.toSet)
}
