package org.elasticsearch.plugin.river.kinesis.parser.impl

import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser
import java.nio.ByteBuffer
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.plugin.river.kinesis.util.Logging
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig

/**
 * Created by JohnDeverna on 8/9/14.
 */
class PassThruDataParser(config: KinesisRiverConfig) extends KinesisDataParser(config) with Logging {

  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  override def processInternal(data: ByteBuffer, req: IndexRequest) = req.source(data)

}
