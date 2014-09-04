# Kinesis River Plugin for Elasticsearch

The Kinesis River Plugin is a river module for [Elasticsearch](elasticsearch.org) that will process records for a particular [AWS Kinesis](aws.amazon.com/kinesis) stream
and bulk load them into the configured index.

At the moment, the data from Kinesis can be in 1 of 2 formats:

* As-Is (direct binary pass-through)
* JSON


### In Progress
This is still a work in progress, although initial tests have been successful


### Install & configure
See the wiki for [installation](github.com/anreved/elasticsearch-kinesis-river/wiki/-Install) and [configuration](github.com/anreved/elasticsearch-kinesis-river/wiki/Configuration)