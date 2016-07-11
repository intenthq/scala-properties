package com.intenthq.properties.apache

import com.intenthq.properties.{PropertyReader, PropertyReaderSpec}
import org.apache.commons.configuration.MapConfiguration

import scala.collection.JavaConversions._

class ApachePropertyReaderSpec extends PropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map =>
    new ApachePropertyReader(new MapConfiguration(map))
}
