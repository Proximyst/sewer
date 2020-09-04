package com.proximyst.sewer;

import com.proximyst.sewer.filtration.FiltrationModule;
import com.proximyst.sewer.piping.PipeResult;
import org.junit.Assert;
import org.junit.Test;

public class FiltrationTest {
  @Test
  public void numberFiltration() {
    SewerSystem<Integer, Integer> system = SewerSystem
        .builder("triple", i -> i * 3, Filters.EVEN.and(Filters.NEGATIVE.not()))
        .pipe("negate", i -> -i, null, Filters.NEGATIVE)
        .build();

    PipeResult<Integer> result = system.pump(15);
    Assert.assertTrue(result.isFiltered());
    Assert.assertTrue(result instanceof PipeResult.PreFilter);
    Assert.assertEquals(result.getPipeName(), "triple");

    result = system.pump(14);
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult().intValue(), -42);

    system = SewerSystem
        .<Integer, Integer>builder("post-filter", Math::abs, null, Filters.NEGATIVE)
        .build();
    result = system.pump(-5);
    Assert.assertTrue(result.isFiltered());
    Assert.assertTrue(result instanceof PipeResult.PostFilter);

    system = SewerSystem.<Integer, Integer>builder("never successful", i -> {
      throw new RuntimeException();
    }).build();

    result = system.pump(1);
    Assert.assertTrue(result.isExceptional());
    Assert.assertTrue(result.asExceptional().getException() instanceof RuntimeException);
  }

  public enum Filters implements FiltrationModule<Integer> {
    EVEN,
    ODD,
    NEGATIVE,
    ;

    @Override
    public boolean allowFlow(Integer integer) {
      return (this == EVEN && integer % 2 == 0)
          || (this == ODD && integer % 2 != 0)
          || (this == NEGATIVE && integer < 0);
    }
  }
}
