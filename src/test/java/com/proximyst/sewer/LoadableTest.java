package com.proximyst.sewer;

import com.proximyst.sewer.loadable.Loadable;
import org.junit.Assert;
import org.junit.Test;

public class LoadableTest {
  @Test
  public void basicLoadable() {
    Loadable<String> loadable = Loadable.of(
        SewerSystem
            .builder("toString", Module.immediatelyWrapping(Integer::toBinaryString))
            .build(),
        7
    );
    Assert.assertFalse(loadable.isLoaded());
    Assert.assertFalse(loadable.getIfPresent().isPresent());
    Assert.assertEquals(loadable.getOrLoad().join().get(), "111");
    Assert.assertTrue(loadable.isLoaded());
    Assert.assertTrue(loadable.getIfPresent().isPresent());
    Assert.assertTrue(loadable.getOrLoad().isDone());
  }
}
