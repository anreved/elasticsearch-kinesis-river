package org.elasticsearch.plugin.river.kinesis.parser

import java.nio.ByteBuffer
import org.elasticsearch.plugin.river.kinesis.exception.PoorlyFormattedDataException
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.plugin.river.kinesis.config.{ElasticsearchConfig, KinesisRiverConfig}
import org.elasticsearch.common.xcontent.XContentType
import java.sql.Timestamp
import java.util.regex.Pattern
import java.text.{DateFormat, SimpleDateFormat}
import org.elasticsearch.plugin.river.kinesis.util.Logging.Log
import java.util.Date

/**
 * Created by JohnDeverna on 8/9/14.
 */
abstract class KinesisDataParser(riverConfig: KinesisRiverConfig) {

  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  def parse(data: ByteBuffer): IndexRequest = {

    // new index request
    val req = new IndexRequest()

    // parse the data internally
    processInternal(data, req)

    // set the index and type (these may depend on values
    req.index(KinesisDataParser.getIndex(riverConfig.elasticsearchConfig, req))
      .`type`(KinesisDataParser.getRecordType(riverConfig.elasticsearchConfig, req))

    // return the request
    req
  }

  @throws[PoorlyFormattedDataException]("if the data cannot be parsed")
  def processInternal(data: ByteBuffer, request: IndexRequest): IndexRequest

}

object KinesisDataParser {

  val dateFormats = Map[String, String](
    "hour" -> "yyyy-MM-dd-HH",
    "day" -> "yyyy-MM-dd",
    "month" -> "yyyy-MM",
    "year" -> "yyyy"
  )

  lazy val formatters = scala.collection.mutable.Map[String, SimpleDateFormat](
    "hour" -> new SimpleDateFormat(dateFormats("hour")),
    "day" -> new SimpleDateFormat(dateFormats("day")),
    "month" -> new SimpleDateFormat(dateFormats("month")),
    "year" -> new SimpleDateFormat(dateFormats("year"))
  )

  val indexRegex = Pattern.compile("^.+\\{(.*?)\\}.*$")

  /**
   * Figure out what the index name should be for this record.  This could be defined in the global config
   * (with the elasticsearch.index property) or could be defined per-record (using the elasticsearch.indexNameField)
   * In either case, the value may need additional parsing to include a timestamped index name (e.g., for daily indexes)
   * @param config the elasticsearch config
   * @param req the index request
   * @return the index name
   */
  def getIndex(config: ElasticsearchConfig, req: IndexRequest): String = {

    val (idx: String, tstamp: Date) =
      (config.indexName, config.indexNameField, config.timestampField) match {

        // indexNameField passed in
        case (_, Some(inf), ts) => {

          // get the request source
          val src = req.sourceAsMap()

          // ensure it contains the configured field name
          assert(src.containsKey(inf), s"indexNameField ${inf} not found in source")

          // return the index name from the source, and the timestamp
          (src.get(inf).toString, getTimestamp(config, req))
        }

        // indexName specified
        case (iname, _, ts) => (iname, getTimestamp(config, req))
      }

    // finally, parse out any timestamped values (e.g., {day})
    parseIndexName(idx, tstamp)
  }

  /**
   * Determine the record type using the configured recordTypeField (i.e., pull from the parsed kinesis data) or
   * use the configured recordType
   * @param req the index request
   * @return the record type
   */
  def getRecordType(config: ElasticsearchConfig, req: IndexRequest): String = config.recordTypeField match {

    // if a field was configured, and the record data has that field defined
    case Some(rtf) => {
      val src = req.sourceAsMap()

      if (src.containsKey(rtf)) {
        src.get(rtf).toString
      }
      else {
        Log.warn("recordTypeField {} not found in source, using default of {}", rtf, config.recordType)
        config.recordType
      }
    }

    // no field configured, use the recordType instead
    case _ => config.recordType
  }


  /**
   * Determine the timestamp for the given record (used for time-based indexes)
   * @param config the elastic search config
   * @param req the index request
   * @return the date for this record
   */
  def getTimestamp(config: ElasticsearchConfig, req: IndexRequest) = {

    if (config.timestampField.isDefined) {
      val src = req.sourceAsMap()

      config.timestampField match {

        // if a timestamp field is configured, let's use it
        case Some(tsf) if (src.containsKey(tsf)) => src.get(tsf) match {
          case a if(a.isInstanceOf[Long]) => new Date(a.asInstanceOf[Long])
          case b if(b.isInstanceOf[String] && config.timestampFormat.isDefined) => {
            val tsFormat = config.timestampFormat.get

            if (formatters.contains(tsFormat)) {
              formatters.get(tsFormat).get.parse(b.toString)
            }
            else {
              val df = new SimpleDateFormat(tsFormat)
              formatters.put(tsFormat, df)
              df.parse(b.toString)
            }
          }
          case c if(c.isInstanceOf[String]) => {
            Log.warn("Timestamp is defined as a String in the source, but no timestampFormat defined in config")
            DateFormat.getInstance().parse(c.toString)
          }
        }

        case Some(tsf) => {
          Log.warn("timestampField was configured as {}, but not found in source.", tsf)
          new Date()
        }

        // otherwise, just return the current date
        case _ => new Date()
      }
    }
    else {
      new Date()
    }
  }


  def parseIndexName(name: String, ts: Date): String = {

    val m = indexRegex.matcher(name)

    m.matches() match {

      // we have a match, let's figure out what date format we need
      case true if (m.groupCount() == 1) => {
        val pattern = m.group(1)
        val dateFormat = pattern match {
          case f if (dateFormats.contains(f)) => formatters(f)
          case f if (formatters.contains(f)) => formatters(f)
          case f => {
            // create a dateformat out of the given pattern
            val df = new SimpleDateFormat(f)

            // add it to the map so we don't create this every time
            formatters.put(f, df)

            // return the date format
            df
          }
        }

        name.replace(s"{${pattern}}", dateFormat.format(ts))
      }

      // doesn't match, no need to parse
      case _ => name
    }
  }

}