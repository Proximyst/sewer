package com.proximyst.sewer;

import com.proximyst.sewer.filtration.ImmediateFiltrationModule;
import com.proximyst.sewer.piping.ImmediatePipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import org.junit.Assert;
import org.junit.Test;

public class FiltrationTest {
  @Test
  public void numberFiltration() {
    SewerSystem<Integer, Integer> system = SewerSystem
        .builder("triple", ImmediatePipeHandler.of(i -> i * 3), Filters.EVEN.and(Filters.NEGATIVE.not()))
        .pipe("negate", ImmediatePipeHandler.of(i -> -i), null, Filters.NEGATIVE)
        .build();

    PipeResult<Integer> result = system.pump(15).join();
    Assert.assertTrue(result.isFiltered());
    Assert.assertTrue(result instanceof PipeResult.PreFilter);
    Assert.assertEquals(result.getPipeName(), "triple");

    result = system.pump(14).join();
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult().intValue(), -42);

    system = SewerSystem
        .<Integer, Integer>builder("post-filter", ImmediatePipeHandler.of(Math::abs), null, Filters.NEGATIVE)
        .build();
    result = system.pump(-5).join();
    Assert.assertTrue(result.isFiltered());
    Assert.assertTrue(result instanceof PipeResult.PostFilter);

    system = SewerSystem
        .<Integer, Integer>builder("never successful", i -> {
          throw new RuntimeException();
        })
        .exceptionHandler(ex -> {
        }).build();

    result = system.pump(1).join();
    Assert.assertTrue(result.isExceptional());
    Assert.assertTrue(result.asExceptional().getException() instanceof RuntimeException);
  }

  public enum Filters implements ImmediateFiltrationModule<Integer> {
    EVEN,
    ODD,
    NEGATIVE,
    ;

    @Override
    public boolean allowFlowImmediately(Integer integer) {
      return (this == EVEN && integer % 2 == 0)
          || (this == ODD && integer % 2 != 0)
          || (this == NEGATIVE && integer < 0);
    }
  }
}
