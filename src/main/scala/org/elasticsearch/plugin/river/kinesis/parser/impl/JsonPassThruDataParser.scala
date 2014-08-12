package org.elasticsearch.plugin.river.kinesis.parser.impl

import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser
import java.nio.ByteBuffer
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import java.nio.charset.{Charset, CharsetDecoder, CharacterCodingException}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.plugin.river.kinesis.util.Logging
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig

/**
 * Created by JohnDeverna on 8/9/14.
 */
class JsonPassThruDataParser(config: KinesisRiverConfig) extends KinesisDataParser(config) with Logging {

  val decoder : CharsetDecoder = Charset.forName("UTF-8").newDecoder();

  @throws[PoorlyFormattedDataException]("if the data cannot be processed")
  override def processInternal(data: ByteBuffer, indexRequest: IndexRequest) = {

    try {
      val json = decoder.decode(data).toString()
      indexRequest.source(json)
    }
    catch {
      case e: CharacterCodingException => {
        Log.error("Malformed data: " + data, e);
        throw PoorlyFormattedDataException(e)
      }
    }
  }
}