#Kinesis River Plugin for ElasticSearch

The Kinesis River Plugin is a river module for Elasticsearch that will process records for a particular [AWS Kinesis](aws.amazon.com/kinesis) stream
and bulk load them into the configured index.

At the moment, the data from Kinesis can be in 1 of 2 formats:
*As-Is (direct binary pass-through)
*JSON


###In Progress
This is still a work in progress, although initial tests have been successful


###Building
You'll need Gradle to build and package the


###Installing the Plugin
To install the plugin, you'll run `gradle bundle` from the project directory.  Then, from `ELASTICSEARCH_HOME` you would
run `./bin/plugin --install kinesis-river --url file:///path/to/projectDir/target/distributions/elastic-kinesis.zip`.
After [re]starting elasticsearch, you could configure the plugin (see Sample Config below).


###Sample River Config
```
curl -XPUT 'localhost:9200/_river/my_kinesis_river_0/_meta' -d '{
    "type" : "kinesis",
    "kinesis" : {
        "region" : "us-east-1",
        "applicationName" : "my_kinesis_application",
        "initialPosition" : "latest",
        "streamName" : "KinesisStreamName",
        "numShards" : 1,
        "createIfNotExists" : true
    },
    "aws" : {
        "accessKey" : "myAccessKey",
        "secretKey" : "mySecretKey"
    },
    "elasticsearch" : {
        "index" : "metrics-{YYYYMMDD}",
        "type" : "myMetricType",
        "maxBulkSize" : 20000,
        "refreshOnBulk" : true,
        "bulkTimeout" : "1000ms",
        "replicationType" : "default",
        "consistencyLevel" : "default"
    },
    "parser":{
        "class": "org.elasticsearch.plugin.river.kinesis.parser.impl.JsonPassThruDataParser",
        "encoding": "UTF-8"
    }
}'
```