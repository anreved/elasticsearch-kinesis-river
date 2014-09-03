package org.elasticsearch.plugin.river.kinesis

import junit.framework.TestCase
import org.junit.{Assert, Before}
import org.elasticsearch.common.inject.{Injector, Guice}
import org.elasticsearch.plugin.river.kinesis.mock.{UnitTestModule, MockElasticSearchModule}
import org.elasticsearch.plugin.river.kinesis.util.Logging
import org.elasticsearch.plugin.river.kinesis.config.{ElasticsearchConfig, KinesisRiverConfig}

/**
 * Created by JohnDeverna on 9/3/14.
 */
abstract class BaseTest extends TestCase with Logging  {

  var injector: Injector = _

  def initInjector = {
    // create the guice injector
    injector = Guice.createInjector(new MockElasticSearchModule, new UnitTestModule)
  }

  @Before
  override def setUp = {
      initInjector
  }


  def getEsConfig: ElasticsearchConfig = injector.getInstance(classOf[KinesisRiverConfig]).elasticsearchConfig


}

