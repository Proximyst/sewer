package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * The result of a pipe's flow.
 *
 * @param <T> The output type.
 * @since 0.1.0
 */
public abstract class PipeResult<T> {
  /**
   * @return Whether the pipe or module flow was successful.
   */
  @Pure
  public abstract boolean isSuccessful();

  /**
   * @return The result type as an {@link Optional}.
   * @since 0.4.0
   */
  @SideEffectFree
  public abstract @NonNull Optional<@NonNull T> asOptional();

  /**
   * @return Whether the system or pipe may continue its operation.
   * @since 0.7.0
   */
  @Pure
  public boolean mayContinue() {
    return isSuccessful();
  }
}
