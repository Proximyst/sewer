package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * The result of a pipe's flow.
 *
 * @param <T> The output type of this pipe.
 */
public abstract class PipeResult<T> {
  /**
   * @return Whether the pipe flow was successful.
   */
  @Pure
  public abstract boolean isSuccessful();

  @SideEffectFree
  public abstract @NonNull Optional<@NonNull T> asOptional();

  @Pure
  public boolean mayContinue() {
    return isSuccessful();
  }
}
