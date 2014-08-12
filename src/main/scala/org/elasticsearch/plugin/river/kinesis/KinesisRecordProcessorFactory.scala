package org.elasticsearch.plugin.river.kinesis

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory
import org.elasticsearch.client.Client
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParserFactory
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig

/**
 * Created by JohnDeverna on 8/8/14.
 */
case class KinesisRecordProcessorFactory(client: Client,
                                         config: KinesisRiverConfig,
                                         parserFactory: KinesisDataParserFactory)
  extends IRecordProcessorFactory {

  /**
   * Create a record processor, passing along the ES client and the data parser factory
   * @return the kinesis record processor
   */
  override def createProcessor() = KinesisRecordProcessor(client, config, parserFactory)

}