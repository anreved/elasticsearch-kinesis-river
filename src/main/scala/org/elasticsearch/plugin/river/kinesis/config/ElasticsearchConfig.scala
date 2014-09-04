package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.action.support.replication.ReplicationType
import org.elasticsearch.action.WriteConsistencyLevel

/**
 * Configuration for elasticsearch
 * @param indexName the indexName
 * @param indexNameField the name of the source field to use as the index name
 * @param timestampField the name of the source field for timestamp
 * @param recordType the type of record
 * @param recordTypeField the name of the source field for record type
 * @param maxBulkSize the max number of items before we execute a bulk
 * @param refreshOnBulk true to refresh after a bulk operation, false otherwise
 * @param bulkTimeout the timeout to use for bulk operations
 * @param replicationType the replication type
 * @param consistencyLevel the consistency level
 *
 * Created by JohnDeverna on 8/12/14.
 */
case class ElasticsearchConfig(indexName: String,
                               indexNameField: Option[String] = None,
                               timestampField: Option[String] = None,
                               timestampFormat: Option[String] = None,
                               recordType: String,
                               recordTypeField: Option[String] = None,
                               maxBulkSize: Int = 20000,
                               refreshOnBulk: Boolean = true,
                               bulkTimeout: String = "1m",
                               replicationType: ReplicationType = ReplicationType.DEFAULT,
                               consistencyLevel: WriteConsistencyLevel = WriteConsistencyLevel.DEFAULT)


/**
 * The Elasticsearch Config companion
 */
object ElasticsearchConfig extends Config[ElasticsearchConfig] {

  val defaultIndex = "kinesis-river-{day}"
  val defaultRecordType = "kinesis-stream"
  val defaultMaxBulkSize = 20000
  val defaultRefreshOnBulk = true
  val defaultBulkTimeout = "1m"
  val defaultReplicationType = "default"
  val defaultConsistencyLevel = "default"

  /**
   * Constructor
   * @param settings the RiverSettings
   * @return an ElasticsearchConfig instance with the parsed configs
   */
  def apply(settings: RiverSettings) = {

    getConfigMap(settings, "elasticsearch") match {
      case Some(es) => new ElasticsearchConfig(
        indexName = getAsOpt(es, "index").getOrElse(defaultIndex),
        indexNameField = getAsOpt(es, "indexNameField"),

        recordType = getAsOpt(es, "type").getOrElse(defaultRecordType),
        recordTypeField = getAsOpt(es, "typeField"),

        timestampField = getAsOpt(es, "timestampField"),
        timestampFormat = getAsOpt(es, "timestampFormat"),

        maxBulkSize = getAsOrElse(es, "maxBulkSize", defaultMaxBulkSize),
        refreshOnBulk = getAsOrElse(es, "refreshOnBulk", defaultRefreshOnBulk),
        bulkTimeout = getAsOrElse(es, "bulkTimeout", defaultBulkTimeout),

        replicationType = ReplicationType.fromString(getAsOrElse(es, "replicationType", defaultReplicationType)),
        consistencyLevel = WriteConsistencyLevel.fromString(getAsOrElse(es, "consistencyLevel", defaultConsistencyLevel))
      )
      case _ => new ElasticsearchConfig(indexName = defaultIndex, recordType = defaultRecordType)
    }
  }
}