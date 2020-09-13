package com.proximyst.sewer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;

public class FutureTest {
  @Test
  public void testBasicFuture() {
    SewerSystem<Integer, Long> system = SewerSystem
        .<Integer, Long>builder("cast", i -> CompletableFuture.supplyAsync(() -> (long) i))
        .build();
    Assert.assertEquals(
        system.pump(123).join().asSuccess().getResult().longValue(),
        123L
    );
    Assert.assertEquals(
        system.pump(36, Executors.newSingleThreadExecutor()).thenApply(l -> l.asSuccess().getResult() * 2).join().longValue(),
        72L
    );
  }
}
