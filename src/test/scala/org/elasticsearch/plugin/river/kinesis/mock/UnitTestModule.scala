package org.elasticsearch.plugin.river.kinesis.mock

import org.elasticsearch.common.inject.AbstractModule
import com.amazonaws.auth.AWSCredentials
import org.elasticsearch.plugin.river.kinesis.util.{KinesisUtil, AwsCredentialsProvider}
import org.elasticsearch.plugin.river.kinesis.parser.{KinesisDataParserProvider, KinesisDataParser}
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory
import org.elasticsearch.plugin.river.kinesis.processor.KinesisRecordProcessorFactory
import org.elasticsearch.river.River
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.plugin.river.kinesis.worker.KinesisWorker

/**
 * Created by JohnDeverna on 9/3/14.
 */
class UnitTestModule extends AbstractModule {

  /**
   * {@inheritdoc}
   */
  @Override
  protected def configure() = {
    // bind config and aws credentials
    bind(classOf[KinesisRiverConfig]).asEagerSingleton()
    bind(classOf[AWSCredentials]).toProvider(classOf[AwsCredentialsProvider]).asEagerSingleton()

    // bind our data parser provider and record processor factory
    bind(classOf[KinesisDataParser]).toProvider(classOf[KinesisDataParserProvider])
    bind(classOf[IRecordProcessorFactory]).to(classOf[KinesisRecordProcessorFactory])

    // setup our kinesis util and worker
    bind(classOf[KinesisUtil])
    bind(classOf[KinesisWorker])

    // finally, setup our river
    bind(classOf[River]).to(classOf[MockKinesisRiver]).asEagerSingleton()
  }
}
