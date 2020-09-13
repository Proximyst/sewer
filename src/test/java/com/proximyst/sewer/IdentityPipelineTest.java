package com.proximyst.sewer;

import com.proximyst.sewer.piping.ImmediatePipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import org.junit.Assert;
import org.junit.Test;

public class IdentityPipelineTest {
  private static <T> ImmediatePipeHandler<T, T> identity() {
    return input -> input;
  }

  @Test
  public void stringIdentity() {
    PipeResult<String> result = SewerSystem.<String, String>builder("identity", identity())
        .build()
        .pump("cool")
        .join();
    Assert.assertTrue(result.isSuccessful());
    Assert.assertEquals(result.asSuccess().getResult(), "cool");
    Assert.assertNotEquals(result.asSuccess().getResult(), "not cool");
    Assert.assertThrows(ClassCastException.class, result::asExceptional);
  }

  @Test
  public void testClassIdentity() {
    TestClass testClass = new TestClass("woah there, cowboy!");
    PipeResult<TestClass> result = SewerSystem.<TestClass, TestClass>builder("identity", identity())
        .build()
        .pump(testClass)
        .join();
    Assert.assertTrue(result.isSuccessful());
    Assert.assertSame(result.asSuccess().getResult(), testClass);
    Assert.assertSame(result.asSuccess().getResult().string, testClass.string);
    Assert.assertNotSame(result.asSuccess().getResult(), new TestClass("woah there, cowboy!"));
    Assert.assertThrows(ClassCastException.class, result::asExceptional);
  }

  public static class TestClass {
    public final String string;

    public TestClass(String string) {
      this.string = string;
    }
  }
}
