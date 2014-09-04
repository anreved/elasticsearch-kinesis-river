package org.elasticsearch.plugin.river.kinesis.worker

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import org.elasticsearch.common.inject.{Singleton, Inject}
import org.elasticsearch.plugin.river.kinesis.util.{Logging, KinesisUtil}
import org.elasticsearch.plugin.river.kinesis.processor.KinesisRecordProcessorFactory

/**
 * A wrapper around the kinesis worker. This allows us to only initialize the worker
 * if we really want to (i.e., not in unit tests)
 * @param recordProcessorFactory the processor factory
 * @param kinesisUtil the kinesis util
 *
 * Created by JohnDeverna on 8/28/14.
 */
@Singleton
class KinesisWorker @Inject() (recordProcessorFactory: KinesisRecordProcessorFactory,
                               kinesisUtil: KinesisUtil) extends Logging {

  /**
   * The wrapped worker
   */
  lazy val workerOpt: Option[Worker] = Some(new Worker(recordProcessorFactory, kinesisUtil.createClientLibraryConfig))
}