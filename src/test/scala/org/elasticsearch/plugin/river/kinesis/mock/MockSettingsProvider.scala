package org.elasticsearch.plugin.river.kinesis.mock

import org.elasticsearch.common.settings.{ImmutableSettings, Settings}
import org.elasticsearch.common.inject.{Singleton, Provider}

/**
 * Created by JohnDeverna on 9/2/14.
 */
@Singleton
class MockSettingsProvider() extends Provider[Settings] {

  val builder = ImmutableSettings.builder()

  System.getProperty("es.properties.file", "_") match {
    case f if (!f.equals("_")) => builder.loadFromSource(f)
    case _ => // do nothing
  }

  override def get() = {
    builder.build()
  }
}