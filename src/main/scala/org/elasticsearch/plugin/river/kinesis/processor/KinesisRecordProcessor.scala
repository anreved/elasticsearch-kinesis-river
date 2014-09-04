package org.elasticsearch.plugin.river.kinesis.processor

import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessorCheckpointer, IRecordProcessor}
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason
import java.util.concurrent.atomic.AtomicLong
import com.amazonaws.services.kinesis.clientlibrary.exceptions.{InvalidStateException, ThrottlingException, ShutdownException}

import org.elasticsearch.action.bulk.{BulkResponse, BulkRequestBuilder}
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import org.elasticsearch.plugin.river.kinesis.util.Logging
import org.elasticsearch.client.Client
import scala.collection.JavaConversions._
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.Provider
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser

/**
 * Created by JohnDeverna on 8/8/14.
 */
/**
 * The implementation of IRecordProcessor required to process records from a Kinesis stream
 * @param client The Elasticsearch client
 * @param config The river config
 * @param dataParserProvider The data parser provider
 */
case class KinesisRecordProcessor(client: Client,
                                  config: KinesisRiverConfig,
                                  dataParserProvider: Provider[KinesisDataParser])

  extends IRecordProcessor with Logging {

  /**
   * Keeps track of the next time we need to checkpoint (every 60 seconds)
   */
  private val nextCheckpointTimeInMillis = new AtomicLong(
    System.currentTimeMillis() + KinesisRecordProcessor.CHECKPOINT_INTERVAL_MILLIS
  )

  /**
   * the shard Id --- this must be a "var" since the KCL passes it in the initialize method
   */
  var shardId: String = "";

  /**
   * Initialize the record processor
   * @param shard the shard id
   */
  override def initialize(shard: String) = {
    Log.info("Initializing Kinesis record processor for shard: {}", shard)
    shardId = shard;
  }

  /**
   * On shutdown, see if we need to do anything
   * @param checkpointer the kinesis checkpointer
   * @param reason the reason for shutdown
   */
  override def shutdown(checkpointer: IRecordProcessorCheckpointer, reason: ShutdownReason) = {

    Log.info("Shutting down record processor for shard: {}, reason: {}", shardId, reason.name());

    // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
    if (ShutdownReason.TERMINATE.equals(reason)) {
      checkpoint(checkpointer);
    }
  }


  /**
   * Process the list of records we got from the stream
   * @param records the records to process
   * @param checkpointer the checkpointer
   */
  override def processRecords(records: java.util.List[Record], checkpointer: IRecordProcessorCheckpointer) {

    Log.info("Processing {} record(s) from shard {}", records.size().toString, shardId)

    // Process records and perform all exception handling.
    processRecordsInternal(records);

    // Checkpoint once every checkpoint interval.
    if (System.currentTimeMillis() > nextCheckpointTimeInMillis.get()) {
      Log.info("Check-pointing with Kinesis")
      checkpoint(checkpointer)
      nextCheckpointTimeInMillis.set(System.currentTimeMillis() + KinesisRecordProcessor.CHECKPOINT_INTERVAL_MILLIS)
    }
  }

  /**
   * Execute the bulk index request
   * @param bulkBuilder the bulk builder
   */
  private def performBulkRequest(bulkBuilder: BulkRequestBuilder): Unit = {

    Log.debug("Executing bulk indexing request for {} item(s)", bulkBuilder.numberOfActions().toString)

    val response: BulkResponse = bulkBuilder.get()

    Log.debug("KinesisRiver indexed {} of {} items and took {}",
      response.getItems.size.toString,
      bulkBuilder.numberOfActions().toString,
      response.getTook.toString)

    if (response.hasFailures) {
      Log.error("Failures processing Kinesis stream \n {}", response.buildFailureMessage())
    }
  }

  private def createBulkBuilder = {
    new BulkRequestBuilder(client)
      .setRefresh(config.elasticsearchConfig.refreshOnBulk)
      .setTimeout(config.elasticsearchConfig.bulkTimeout)
      .setReplicationType(config.elasticsearchConfig.replicationType)
      .setConsistencyLevel(config.elasticsearchConfig.consistencyLevel)
  }

  /**
   * Process records performing retries as needed.
   * @param records the records to process
   */
  private def processRecordsInternal(records: java.util.List[Record]) : Unit = {

    // we want to do a bulk index operation
    var bulkBuilder = createBulkBuilder

    val recordList = asScalaBuffer(records)

    Log.info("Starting to process {} kinesis records", recordList.size.toString)

    recordList.foreach(record => {
      processSingleRecord(record, bulkBuilder)

      // if we've reached the max size for bulk actions,
      if(bulkBuilder.numberOfActions() >= config.elasticsearchConfig.maxBulkSize) {

        Log.info("Reached max bulk request size at {} items.", bulkBuilder.numberOfActions().toString)

        // execute the batch
        performBulkRequest(bulkBuilder)

        // reset for a new batch of index requests
        bulkBuilder = createBulkBuilder
      }
    })

    // process any remaining (that didn't go over max bulk size)
    if (bulkBuilder.numberOfActions() > 0) {
      Log.info("Bulk reqeust has {} item(s) in it, sending for indexing", bulkBuilder.numberOfActions().toString)
      performBulkRequest(bulkBuilder)
    }
  }

  /**
   * Actual processing of a single data record
   * @param record The record
   * @param builder The BulkRequestBuilder
   * @param attempt which attempt we're on
   * @return Either a boolean (true if processed, false if failed) or exception
   */
  def processSingleRecord(record: Record, builder: BulkRequestBuilder, attempt: Int = 0): Either[Boolean, Exception] = {

    // internal retry method
    def retry = {

      // if we've tried too many times, just end it here
      if(attempt > KinesisRecordProcessor.NUM_RETRIES-1) {
        Left(false);
      }

      // otherwise, we'll wait a few seconds then try again
      else {
        try {
          Thread.sleep(KinesisRecordProcessor.BACKOFF_TIME_IN_MILLIS)
          processSingleRecord(record, builder, attempt + 1)
        }
        catch {
          case e: Exception => {
            Log.error("Error processing single record", e)
            Right(e)
          }
        }
      }
    }


    try {
      // get the data and do something with it
      val raw = record.getData;

      Log.info("Got raw record data from kinesis")

      // parse the data and add to the bulk loader
      builder.add(dataParserProvider.get().parse(raw))

      Log.info("Added record to bulk indexing queue")

      // return success
      Left(true)
    }
    catch {
      // if data is poorly formatted, there is nothing to retry
      case p: PoorlyFormattedDataException => {
        Log.error("Unable to parse data from Kinesis stream", p)
        Right(p)
      }

      // any other exception, try again
      case e: Exception => {
        Log.error("Error adding record to builder", e)
        retry
      }
    }
  }

  /**
   * Checkpoint with kinesis -- retry if needed
   * @param checkpointer the checkpointer
   */
  private def checkpoint(checkpointer: IRecordProcessorCheckpointer): Unit = {

    import scala.util.control.Breaks._

    Log.info("Checkpointing s hard {}", shardId);

    for (i <- 0 to KinesisRecordProcessor.NUM_RETRIES) {
      try {
        checkpointer.checkpoint();
        break;
      }
      catch {
        case se: ShutdownException => {
          // Ignore checkpoint if the processor instance has been shutdown (fail over).
          Log.info("Caught shutdown exception, skipping checkpoint.", se);
          break;
        }
        case e: ThrottlingException => {
          // Backoff and re-attempt checkpoint upon transient failures
          if (i >= (KinesisRecordProcessor.NUM_RETRIES - 1)) {
            Log.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
            break;
          }
          else {
            Log.info("Transient issue when checkpointing - attempt " + (i + 1) + " of " + KinesisRecordProcessor.NUM_RETRIES, e);
          }
        }
        case e: InvalidStateException => {
          // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
          Log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
          break;
        }
      }

      try {
        Thread.sleep(KinesisRecordProcessor.BACKOFF_TIME_IN_MILLIS);
      }
      catch {
        case e: InterruptedException => {
          Log.debug("Interrupted sleep", e);
        }
      }
    }
}

}


/**
 * KinesisRecordProcessor companion
 */
object KinesisRecordProcessor {

  // Backoff and retry settings
  private val BACKOFF_TIME_IN_MILLIS = 3000L

  // max number of times to retry
  private val NUM_RETRIES = 3

  // Checkpoint about once a minute
  private val CHECKPOINT_INTERVAL_MILLIS = 60000L;
}