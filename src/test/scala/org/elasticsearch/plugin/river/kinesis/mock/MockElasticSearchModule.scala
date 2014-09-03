package org.elasticsearch.plugin.river.kinesis.mock

import org.elasticsearch.common.inject.{Singleton, AbstractModule}
import org.elasticsearch.river.{RiverSettings, RiverName}
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings

/**
 * This replaces the setup you would normally get when running inside elasticsearch
 * Created by JohnDeverna on 9/2/14.
 */
@Singleton
class MockElasticSearchModule extends AbstractModule {

  protected override def configure() = {
    bind(classOf[RiverName]).toInstance(new RiverName("kinesis", "kinesis-river"))
    bind(classOf[Client]).to(classOf[MockClient])
    bind(classOf[Settings]).toProvider(classOf[MockSettingsProvider])
    bind(classOf[RiverSettings]).toProvider(classOf[MockRiverSettingsProvider])
  }
}