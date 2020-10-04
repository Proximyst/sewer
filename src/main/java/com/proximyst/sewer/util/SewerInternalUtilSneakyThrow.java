package com.proximyst.sewer.util;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An internal class for sewer to sneaky throw {@link Throwable}s.
 *
 * @deprecated This is an internal class and should not be used.
 */
@SuppressWarnings("DeprecatedIsStillUsed") // Only deprecated to warn users.
@Deprecated
public final class SewerInternalUtilSneakyThrow {
  private SewerInternalUtilSneakyThrow() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  /**
   * Sneaky throw the throwable.
   *
   * @param throwable The throwable to rethrow.
   */
  @SuppressWarnings("RedundantTypeArguments")
  public static void sneakyThrow(@NonNull final Throwable throwable) {
    throw SewerInternalUtilSneakyThrow.<RuntimeException>superSneaky(throwable);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T superSneaky(@NonNull final Throwable throwable) throws T {
    throw (T) throwable;
  }
}
