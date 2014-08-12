package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.common.inject.AbstractModule
import org.elasticsearch.river.River

/**
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiverModule extends AbstractModule {

  /**
   * {@inheritdoc}
   */
  @Override
  protected def configure() = {
    bind(classOf[River]).to(classOf[KinesisRiver]).asEagerSingleton();
  }

}