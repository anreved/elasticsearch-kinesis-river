package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.common.inject.{AbstractModule, Inject}
import org.elasticsearch.river.{RiverSettings, RiverName, River, AbstractRiverComponent}
import org.elasticsearch.client.Client
import org.elasticsearch.plugin.river.kinesis.util.{Logging, KinesisUtil}
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.plugin.river.kinesis.worker.KinesisWorker

/**
 * The River
 * @param riverName The river name
 * @param settings The global settings
 * @param client The elasticsearch client
 * @param riverConfig The river config/settings
 * @param kinesisUtil The kinesis util
 * @param kinesisWorker The kinesis worker
 *
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiver @Inject() (riverName: RiverName,
                              settings: RiverSettings,
                              client: Client,
                              riverConfig: KinesisRiverConfig,
                              kinesisUtil: KinesisUtil,
                              kinesisWorker: KinesisWorker
                             )
  extends AbstractRiverComponent(riverName, settings)
     with River
     with Logging {

  /**
   * {@inheritdoc}
   */
  override def start() = {

    // initialize the client
    kinesisUtil.initKinesisClient

    // ensure the shard exists and is active
    kinesisUtil.createStreamIfNotExists()

    try {
      Log.info("Starting Kinesis worker")
      kinesisWorker.workerOpt.get.run()
    }
    catch {
      case t: Throwable => {
        Log.error("Caught throwable while processing data.", t);
      }
    }
  }

  /**
   * {@inheritdoc}
   */
  override def close() = {

    Log.info("Shutting down Kinesis worker")

    // shut it down
    kinesisWorker.workerOpt match {
      case Some(wrk) => wrk.shutdown()
      case _ => Log.error("No kinesis worker found to shutdown")
    }
  }
}