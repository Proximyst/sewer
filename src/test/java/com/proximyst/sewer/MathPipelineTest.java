package com.proximyst.sewer;

import com.proximyst.sewer.piping.PipeResult;
import org.junit.Assert;
import org.junit.Test;

public class MathPipelineTest {
  @Test
  public void multiplication() {
    PipeResult<Long> result = SewerSystem.<Integer, Integer>builder("double", i -> i * 2)
        .pipe("square", i -> (long) i * i)
        .build()
        .pump(123);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult().longValue(), 60516L);
    Assert.assertThrows(ClassCastException.class, result::asExceptional);
  }

  @Test
  public void multiplicationWithInnerPipeline() {
    PipeResult<Long> result = SewerSystem.<Integer, Long>builder("double", i -> i * 2L)
        .pipe("third", i -> i / 3)
        .pipe("square", SewerSystem.<Long, Long>builder("triple", i -> i * 3)
            .pipe("square", i -> i * i)
            .build())
        .build()
        .pump(123);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult().longValue(), 60516L);
    Assert.assertThrows(ClassCastException.class, result::asExceptional);
  }
}
