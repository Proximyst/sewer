package com.proximyst.sewer;

import com.proximyst.sewer.piping.ImmediatePipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import org.junit.Assert;
import org.junit.Test;

public class MathPipelineTest {
  @Test
  public void multiplication() {
    PipeResult<Long> result = SewerSystem.<Integer, Integer>builder("double", ImmediatePipeHandler.of(i -> i * 2))
        .pipe("square", ImmediatePipeHandler.of(i -> (long) i * i))
        .build()
        .pump(123)
        .join();
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult().longValue(), 60516L);
    Assert.assertThrows(ClassCastException.class, result::asExceptional);
  }
}
