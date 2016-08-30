package com.intenthq.properties.hadoop

import com.intenthq.properties.PropertyReader
import org.apache.hadoop.conf.Configuration

import scala.util.Try

class HadoopPropertyReader(val config: Configuration) extends PropertyReader {
  override def getAsString(name: String): Option[String] =
    Try(Option(config.get(name))).toOption.flatten.filterNot(_ == "")
}
