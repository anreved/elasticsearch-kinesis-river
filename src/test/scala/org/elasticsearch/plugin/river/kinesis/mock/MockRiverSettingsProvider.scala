package org.elasticsearch.plugin.river.kinesis.mock

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.common.inject.{Singleton, Inject, Provider}
import org.elasticsearch.common.settings.{ImmutableSettings, Settings}
import org.elasticsearch.plugin.river.kinesis.util.Logging.Log
import scala.io.Source
import scala.collection.JavaConversions._

/**
 * Created by JohnDeverna on 9/2/14.
 */
@Singleton
class MockRiverSettingsProvider @Inject()(globalSettings: Settings) extends Provider[RiverSettings] {

  val builder = ImmutableSettings.builder()

  // see if a property file was specified in the env
  System.getProperty("river.properties.file", "_") match {
    case f if(!f.equals("_")) => {
      Log.info("Loading elasticsearch properties from {}", f)
      builder.loadFromSource( Source.fromFile(f).getLines().mkString )
    }
    case _ => // do nothing
  }

  // override any river settings with whatever is in the system properties
  for (key: String <- asScalaSet(System.getProperties.stringPropertyNames)) {
    if (key.startsWith("river.override.")) {
      builder.put(key.replace("river.override.", ""), System.getProperty(key))
    }
  }

  override def get() = {
    new RiverSettings(globalSettings, builder.build().getAsStructuredMap)
  }
}