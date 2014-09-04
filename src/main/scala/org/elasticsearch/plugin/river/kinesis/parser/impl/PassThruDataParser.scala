package org.elasticsearch.plugin.river.kinesis.parser.impl

import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser
import java.nio.ByteBuffer
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.plugin.river.kinesis.util.Logging
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.{Singleton, Inject}

/**
 * Created by JohnDeverna on 8/9/14.
 */
/**
 * Basic data parser that simply passes the raw kinesis date along to elasticsearch.  There is no processing done.
 * @param config The river config
 */
@Singleton
class PassThruDataParser @Inject()(config: KinesisRiverConfig) extends KinesisDataParser(config) with Logging {

  /**
   * Unwraps the raw data into a byte[] and tries to set it directly on the IndexRequest
   * @param data The raw kinesis data
   * @param req The indexRequest
   * @throws PoorlyFormattedDataException if the data doesn't fit nicely in the IndexRequest
   */
  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  override def processInternal(data: ByteBuffer, req: IndexRequest) = req.source(data.array())

}