package org.elasticsearch.plugin.river.kinesis.worker

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import org.elasticsearch.common.inject.{Singleton, Inject}
import org.elasticsearch.plugin.river.kinesis.util.{Logging, KinesisUtil}
import org.elasticsearch.plugin.river.kinesis.processor.KinesisRecordProcessorFactory

/**
 * Created by JohnDeverna on 8/28/14.
 */
@Singleton
class KinesisWorker @Inject() (recordProcessorFactory: KinesisRecordProcessorFactory,
                               kinesisUtil: KinesisUtil) extends Logging {

  lazy val workerOpt: Option[Worker] = Some(new Worker(recordProcessorFactory, kinesisUtil.createClientLibraryConfig))
}
