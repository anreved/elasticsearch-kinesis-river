package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.action.support.replication.ReplicationType
import org.elasticsearch.action.WriteConsistencyLevel

/**
 * Created by JohnDeverna on 8/12/14.
 */
case class ElasticsearchConfig(indexName: String = "kinesis-river-{day}",
                               recordType: String = "kinesis-river",
                               maxBulkSize: Int = 20000,
                               refreshOnBulk: Boolean = true,
                               bulkTimeout: String = "1m",
                               replicationType: ReplicationType = ReplicationType.DEFAULT,
                               consistencyLevel: WriteConsistencyLevel = WriteConsistencyLevel.DEFAULT) {

  def getindex: String = {
    indexName
  }
}

object ElasticsearchConfig extends Config[ElasticsearchConfig] {

  def apply(settings: RiverSettings) = {

    settings.settings().containsKey("elasticsearch") match {
      case true => {
        val es = settings.settings().get("elasticsearch").asInstanceOf[Map[String, AnyRef]]

        new ElasticsearchConfig(
          indexName = getAsOrElse(es, "index", "kinesis-river-{day}"),
          recordType = getAsOrElse(es, "type", "kinesis-stream"),
          maxBulkSize = getAsOrElse(es, "maxBulkSize", 20000),
          refreshOnBulk = getAsOrElse(es, "refreshOnBulk", true),
          bulkTimeout = getAsOrElse(es, "bulkTimeout", "1m"),
          replicationType = ReplicationType.fromString(getAsOrElse(es, "replicationType", "default")),
          consistencyLevel = WriteConsistencyLevel.fromString(getAsOrElse(es, "consistencyLevel", "default"))
        )
      }
      case _ => new ElasticsearchConfig()
    }
  }
}