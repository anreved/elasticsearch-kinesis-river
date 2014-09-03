package org.elasticsearch.plugin.river.kinesis.processor

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory
import org.elasticsearch.client.Client
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.{Singleton, Inject, Provider}
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser

/**
 * Created by JohnDeverna on 8/8/14.
 */
@Singleton
class KinesisRecordProcessorFactory @Inject() (client: Client,
                                               config: KinesisRiverConfig,
                                               parserProvider: Provider[KinesisDataParser])
  extends IRecordProcessorFactory {

  /**
   * Create a record processor, passing along the ES client and the data parser factory
   * @return the kinesis record processor
   */
  override def createProcessor() = KinesisRecordProcessor(client, config, parserProvider)
}