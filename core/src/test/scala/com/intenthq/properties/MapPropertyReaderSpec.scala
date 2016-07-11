package com.intenthq.properties

import java.util.Properties

class MapPropertyReaderSpec extends PropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = new MapPropertyReader(_)
}

class SystemPropertyReaderSpec extends PropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map => {
    val props = new Properties()
    map.foreach { case (k, v) => props.setProperty(k, v) }
    new SystemPropertyReader(props)
  }
}

class CombinedPropertyReaderSpec extends PropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map => {
    val first = map.take(map.size / 2)
    val second = map.drop(map.size / 2)

    CombinedReader(new MapPropertyReader(first), new MapPropertyReader(second))
  }
}
