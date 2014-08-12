package org.elasticsearch.plugin.river.kinesis.parser

import java.nio.ByteBuffer
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig

/**
 * Created by JohnDeverna on 8/9/14.
 */
abstract class KinesisDataParser(riverConfig: KinesisRiverConfig) {

  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  def parse(data: ByteBuffer) : IndexRequest = {

    val req = new IndexRequest(riverConfig.elasticsearchConfig.getindex, riverConfig.elasticsearchConfig.recordType)

    processInternal(data, req)
  }

  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  def processInternal(data: ByteBuffer, request: IndexRequest) : IndexRequest
}