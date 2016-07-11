package com.intenthq.properties.hadoop

import com.intenthq.properties.{PropertyReader, PropertyReaderSpec}
import org.apache.hadoop.conf.Configuration

class HadoopPropertyReaderSpec extends PropertyReaderSpec {

  override def initReader: (Map[String, String]) => PropertyReader = map => {
    val conf = new Configuration()
    map.foreach { case (k, v) => conf.set(k, v) }
    new HadoopPropertyReader(conf)
  }
}
