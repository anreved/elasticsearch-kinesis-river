#Kinesis River Plugin for Elasticsearch

The Kinesis River Plugin is a river module for [Elasticsearch](elasticsearch.org) that will process records for a particular [AWS Kinesis](aws.amazon.com/kinesis) stream
and bulk load them into the configured index.

At the moment, the data from Kinesis can be in 1 of 2 formats:

* As-Is (direct binary pass-through)
* JSON


###In Progress
This is still a work in progress, although initial tests have been successful


###Building
You'll need Gradle to build and package the plugin.  From the project directory, simply run `gradle bundle` to create the installable zip file, or run `gradle test` to execute Unit Tests.

###Installing the Plugin
To install the plugin, you need to

1. run `gradle bundle` from the project directory
2. run 
```
./bin/plugin --install kinesis-river \
--url file:///path/to/projectDir/target/distributions/elastic-kinesis.zip
```
3. [re]start elasticsearch, and configure the plugin (see Sample Config below)


###Config Options
The configuration is broken down into four parts.

######Elasticsearch Configuration

| Config Key       | Description    | Required | Default Value |
| -------------    | -------------  | -------------   | ------------- |
| index            | The name of the index in Elasticsearch.  Either this value, or the _indexNameField_ property should be defined  | N   |   |
| indexNameField   | The field in the record source that contains the index name. Either this value or _index_ needs to be defined | N | |
| type             | The item *type*                                            | N | |
| typeField        | The field in the record source that contains the item type | N | |
| timestampField   | The field in the record source that contains the timestamp | N | |
| timestampFormat  | The format for the incoming date (using a valid [Java format](docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html)) | N | |
| maxBulkSize      | The maximum number of records to be included in a single bulk request | N | 20,000 |
| refreshOnBulk    | True to refresh after a bulk operation | N | true |
| bulkTimeout      | The max time to spend before timing out a bulk operation | N | 1 minute |
| replicationType  | The elasticsearch replication type | N | default |
| consistencyLevel | The elasticsearch consistency level | N | default |


######Kinesis Configuration

| Config Key        | Description    | Required        | Default Value |
| -------------     | -------------  | -------------   | ------------- |
| region            | The AWS region | Y               | us-east-1     |
| streamName        | The name of the Kinesis stream | Y | |
| applicationName   | The name of this Kinesis application | N | elasticsearch-kinesis-river  |
| initialPosition   | The initial position to use when reading the stream:  _latest_,_trim_   | N | latest |
| numShards         | The number of shards in the stream | N | 1 |
| createIfNotExists  | True to create the stream if it doesn't already exist | N | true |


###Sample River Config
```json
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