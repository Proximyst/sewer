package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * The result of a pipe's flow.
 *
 * @param <T> The output type.
 */
public abstract class PipeResult<T> {
  /**
   * @return Whether the pipe or module flow was successful.
   */
  @Pure
  public abstract boolean isSuccessful();

  /**
   * @return The result type as an {@link Optional}.
   */
  @SideEffectFree
  public abstract @NonNull Optional<@NonNull T> asOptional();

  /**
   * @return Whether the system or pipe may continue its operation.
   */
  @Pure
  public boolean mayContinue() {
    return isSuccessful();
  }
}
