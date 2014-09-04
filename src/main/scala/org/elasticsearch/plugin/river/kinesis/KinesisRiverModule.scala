package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.common.inject.AbstractModule
import org.elasticsearch.river.River
import org.elasticsearch.plugin.river.kinesis.parser.{KinesisDataParserProvider, KinesisDataParser}
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory
import org.elasticsearch.plugin.river.kinesis.processor.KinesisRecordProcessorFactory
import com.amazonaws.auth.AWSCredentials
import org.elasticsearch.plugin.river.kinesis.util.{KinesisUtil, AwsCredentialsProvider}
import org.elasticsearch.plugin.river.kinesis.worker.KinesisWorker
*/
/**
 * Guice module for the Kinesis River
 *
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiverModule extends AbstractModule {

  /**
   * {@inheritdoc}
   */
  @Override
  protected def configure() = {

    // bind settings, aws credentials
    bind(classOf[KinesisRiverConfig]).asEagerSingleton()
    bind(classOf[AWSCredentials]).toProvider(classOf[AwsCredentialsProvider]).asEagerSingleton()

    // bind our data parser provider and record processor factory
    bind(classOf[KinesisDataParser]).toProvider(classOf[KinesisDataParserProvider])
    bind(classOf[IRecordProcessorFactory]).to(classOf[KinesisRecordProcessorFactory])

    // setup our kinesis util and worker
    bind(classOf[KinesisUtil])
    bind(classOf[KinesisWorker])

    // finally, setup our river
    bind(classOf[River]).to(classOf[KinesisRiver]).asEagerSingleton()
  }

}