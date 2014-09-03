package org.elasticsearch.plugin.river.kinesis.mock

import org.elasticsearch.river.{RiverSettings, RiverName, River, AbstractRiverComponent}
import org.elasticsearch.plugin.river.kinesis.util.{KinesisUtil, Logging}
import org.elasticsearch.client.Client
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.{Singleton, Inject}
import org.elasticsearch.plugin.river.kinesis.worker.KinesisWorker

/**
 * Created by JohnDeverna on 9/2/14.
 */
@Singleton
class MockKinesisRiver @Inject()(riverName: RiverName,
                                 settings: RiverSettings,
                                 client: Client,
                                 riverConfig: KinesisRiverConfig,
                                 kinesisUtil: KinesisUtil,
                                 kinesisWorker: KinesisWorker
                                  ) extends AbstractRiverComponent(riverName, settings)
with River
with Logging {
  override def start() = {
    Log.info("Starting Kinesis River ")
  }

  override def close() = {
    Log.info("Stopping Kinesis River ")
  }
}