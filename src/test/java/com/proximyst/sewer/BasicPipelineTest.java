package com.proximyst.sewer;

import com.proximyst.sewer.piping.NamedPipeResult;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.SuccessfulResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Assert;
import org.junit.Test;

public class BasicPipelineTest {
  @Test
  public void maths() {
    SewerSystem<Long, Long> pipeline = SewerSystem
        .<Long, Long>builder("multiply", Module.immediatelyWrapping(in -> in * 7))
        .module("max amount", Module.filtering(in -> in < 10_000L))
        .module("modulo", Module.immediatelyWrapping(in -> in % 3))
        .build();
    Assert.assertEquals(pipeline.pump(123L).join().asOptional().get(), Long.valueOf(0L));
    Assert.assertTrue(pipeline.pump(246L).join().isSuccessful());
    Assert.assertTrue(pipeline.pump(246L).join().mayContinue());
    Assert.assertFalse(pipeline.pump(5_000L).join().isSuccessful());
    Assert.assertFalse(pipeline.pump(5_000L).join().mayContinue());
    Assert.assertFalse(pipeline.pump(5_000L).join().asOptional().isPresent());
  }

  @Test
  public void sleeping() {
    Executor executor = Executors.newSingleThreadExecutor();
    SewerSystem<Long, Long> pipeline = SewerSystem
        .<Long, Long>builder(
            "sleep & identity",
            in -> CompletableFuture.supplyAsync(() -> {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException ignored) {
              }
              return new SuccessfulResult<>(in);
            }, executor)
        )
        .build();
    CompletableFuture<@NonNull NamedPipeResult<Long, ? extends PipeResult<Long>>> future = pipeline.pump(123L);
    Assert.assertFalse(future.isDone()); // Pre-join
    Assert.assertEquals(future.join().asOptional().get(), Long.valueOf(123L));
    Assert.assertTrue(future.isDone()); // Post-join
  }
}
