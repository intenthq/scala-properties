package com.intenthq.properties

import java.util.Properties

import uscala.result.Result
import uscala.result.Result.Ok

trait PropertyReader {
  def readSafe(name: String): Option[String]

  def orError(name: String): Result[String, String] =
    Result.fromOption(readSafe(name), PropertyReader.MissingValueFormat.format(name))

  def required[T](name: String)(implicit convert: String => Result[String, T]): Result[String, T] =
    orError(name).flatMap(convert)

  def optional[T](name: String)(implicit convert: String => Result[String, T]): Result[String, Option[T]] =
    readSafe(name).map(convert).fold[Result[String, Option[T]]](Ok(None))(_.map(Some(_)))
}

object PropertyReader {
  val MissingValueFormat = "'%s' is missing from the configuration"
}

class MapPropertyReader(val props: Map[String, String]) extends PropertyReader {
  override def readSafe(name: String): Option[String] = props.get(name)
}

class SystemPropertyReader(props: Properties = System.getProperties) extends PropertyReader {
  override def readSafe(name: String): Option[String] = Option(props.getProperty(name)).filterNot(_ == "")
}

class CombinedReader (val readers: Set[PropertyReader]) extends PropertyReader {
  override def readSafe(name: String): Option[String] = readers.flatMap(reader => reader.readSafe(name)).headOption
}

object CombinedReader {
  def apply(readers: PropertyReader*): CombinedReader = new CombinedReader(readers.toSet)
}
