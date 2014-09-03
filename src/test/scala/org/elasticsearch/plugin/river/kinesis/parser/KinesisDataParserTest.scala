package org.elasticsearch.plugin.river.kinesis.parser

import org.elasticsearch.plugin.river.kinesis.BaseTest
import org.junit.{Assert, Test}
import org.elasticsearch.action.index.IndexRequest
import java.util.Date
import java.text.SimpleDateFormat

/**
 * Created by JohnDeverna on 9/3/14.
 */
class KinesisDataParserTest extends BaseTest {


  val INCORRECT_IDX_NAME = "Incorrect index name"

  /**
   * Creates a copy of the ElasticSearch config with the passed values
   * @param idxName the index name
   * @param idxNameField the index name field
   * @param timestampField the timestamp field name
   * @return the elasticsearch config
   */
  def getCfg(idxName: String,
             idxNameField: Option[String] = None,
             timestampField: Option[String] = None,
             timestampFormat: Option[String] = None) = getEsConfig.copy(
    indexName = idxName,
    indexNameField = idxNameField,
    timestampField = timestampField,
    timestampFormat = timestampFormat
  )

  def indexRequest = new IndexRequest()
  def indexRequestWithSource(args: AnyRef*) = indexRequest.source(args:_*)

  /**
   * Simple index name pass-thru
   */
  @Test
  def testIndexName_Simple {

    val indexName = KinesisDataParser.getIndex(getCfg("idxName"), indexRequest)

    Assert.assertEquals(INCORRECT_IDX_NAME, "idxName", indexName)
  }

  /**
   * Make sure that the index name is parsed correctly to include the day
   */
  @Test
  def testIndexName_Day {
    val expectedName = s"idx-${KinesisDataParser.formatters("day").format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg("idx-{day}"), indexRequest)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

  @Test
  def testIndexName_Hour {
    val expectedName = s"idx-${KinesisDataParser.formatters("hour").format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg("idx-{hour}"), indexRequest)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

  @Test
  def testIndexName_Year {
    val expectedName = s"idx-${KinesisDataParser.formatters("year").format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg("idx-{year}"), indexRequest)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

  @Test
  def testIndexName_Custom {

    val dateFormat = "MM.dd.yyyy-HH"
    val sdf = new SimpleDateFormat(dateFormat)

    val expectedName = s"idx-${sdf.format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg(s"idx-{${dateFormat}}"), indexRequest)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }


  @Test
  def testIndexNameFromSource_Simple {
    val idxReq = indexRequestWithSource("indexField", "dynamicIndexName")

    val indexName = KinesisDataParser.getIndex(getCfg("idxName", Some("indexField")), idxReq)

    Assert.assertEquals(INCORRECT_IDX_NAME, "dynamicIndexName", indexName)
  }

  @Test
  def testIndexNameFromSource_Day {

    val idxReq = indexRequestWithSource("indexField", "dynamicIndexName-{day}")

    val expectedName = s"dynamicIndexName-${KinesisDataParser.formatters("day").format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg("idxName", Some("indexField")), idxReq)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

  @Test
  def testIndexNameFromSource_Custom {

    val dateFormat = "MM.dd.yyyy-HH"
    val sdf = new SimpleDateFormat(dateFormat)

    val idxReq = indexRequestWithSource("indexField", s"dynamicIndexName-{${dateFormat}}")

    val expectedName = s"dynamicIndexName-${sdf.format(new Date())}"
    val indexName = KinesisDataParser.getIndex(getCfg("idxName", Some("indexField")), idxReq)

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }


  @Test
  def testIndexNameAndDateFromSource_Day {

    val idxReq = indexRequestWithSource("indexField", "dynamicIndexName-{day}",
                                        "ts", "2014-06-19 23:01:04")

    val expectedName = "dynamicIndexName-2014-06-19"
    val indexName = KinesisDataParser.getIndex(
      getCfg("idxName", Some("indexField"), Some("ts"), Some("yyyy-MM-dd HH:mm:ss")),
      idxReq
    )

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

  @Test
  def testIndexNameAndDateFromSource_Custom {

    val dateFormat = "MM.dd.yyyy-HH"

    val idxReq = indexRequestWithSource("indexField", s"dynamicIndexName-{${dateFormat}}",
                                        "ts", "2014-06-19 23:01:04")

    val expectedName = "dynamicIndexName-06.19.2014-23"
    val indexName = KinesisDataParser.getIndex(
      getCfg("idxName", Some("indexField"), Some("ts"), Some("yyyy-MM-dd HH:mm:ss")),
      idxReq
    )

    Assert.assertEquals(INCORRECT_IDX_NAME, expectedName, indexName)
  }

}