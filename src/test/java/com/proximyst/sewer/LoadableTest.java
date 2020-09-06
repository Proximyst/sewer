package com.proximyst.sewer;

import com.proximyst.sewer.loadable.Loadable;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class LoadableTest {
  @Test
  public void basicLoading() {
    Loadable<String> loadable = Loadable
        .builder(
            SewerSystem.builder("string", name -> "Hello, " + name + "!").build(),
            "Anton"
        )
        .build();

    Assert.assertFalse(loadable.isLoaded());
    Assert.assertTrue(loadable.getIfPresent().isEmpty());
    Assert.assertFalse(loadable.getOrLoad().isDone());
    Assert.assertTrue(loadable.getOrLoad().join().isPresent());
    Assert.assertFalse(loadable.getIfPresent().isEmpty());
    Assert.assertEquals(loadable.getOrLoad().join().get(), "Hello, Anton!");

    Loadable<String> lowercase = Loadable
        .builder(
            SewerSystem
                .<String, String>builder("lowercase", String::toLowerCase)
                .pipe("wait", s -> {
                  Thread.sleep(100);
                  return s;
                })
                .build(),
            loadable
        )
        .build();
    long start = System.nanoTime();
    lowercase.getOrLoad().join();
    long end = System.nanoTime();
    Assert.assertTrue(end - start >= TimeUnit.MILLISECONDS.toNanos(100));
    Assert.assertEquals(lowercase.getOrLoad().join().get(), "hello, anton!");
  }
}
