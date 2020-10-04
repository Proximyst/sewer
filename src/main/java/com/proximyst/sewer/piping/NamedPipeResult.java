package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A wrapper {@link PipeResult} for an inner result, {@link PR}. This stores the name of the pipe the result originates
 * from.
 *
 * @param <T>  The output type of the {@link PipeResult}.
 * @param <PR> The inner {@link PipeResult} whose output type is {@link T}.
 * @since 0.7.0
 */
public final class NamedPipeResult<T, PR extends PipeResult<T>> extends PipeResult<T> {
  /**
   * The name of the pipe the inner result originates from.
   */
  private final @NonNull String pipeName;

  /**
   * The inner result originating from pipe flow.
   */
  private final @NonNull PR result;

  /**
   * @param pipeName The name of the pipe this result originates from.
   * @param result   The wrapped result this represents.
   */
  public NamedPipeResult(@NonNull String pipeName, @NonNull PR result) {
    this.pipeName = pipeName;
    this.result = result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Pure
  public boolean isSuccessful() {
    return this.result.isSuccessful();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Pure
  public boolean mayContinue() {
    return this.result.mayContinue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SideEffectFree
  public @NonNull Optional<@NonNull T> asOptional() {
    return this.result.asOptional();
  }

  /**
   * @return The name of the pipe the {@link #getResult() inner result} originates from.
   */
  @Pure
  public @NonNull String getPipeName() {
    return this.pipeName;
  }

  /**
   * @return The inner {@link PipeResult}.
   */
  @Pure
  public @NonNull PR getResult() {
    return this.result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NamedPipeResult<?, ?> that = (NamedPipeResult<?, ?>) o;
    return getPipeName().equals(that.getPipeName()) &&
        getResult().equals(that.getResult());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(getPipeName(), getResult());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "NamedPipeResult{" +
        "pipeName='" + pipeName + '\'' +
        ", result=" + result +
        '}';
  }
}
