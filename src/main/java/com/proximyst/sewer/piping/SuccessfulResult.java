package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A successful {@link PipeResult}, originating from a successful flow.
 *
 * @param <T> The type of the {@link PipeResult}.
 * @see #getResult()
 * @since 0.7.0
 */
public final class SuccessfulResult<T> extends PipeResult<T> {
  /**
   * The inner item this result has as output.
   */
  private final T result;

  /**
   * @param result The item this result represents.
   */
  public SuccessfulResult(T result) {
    this.result = result;
  }

  /**
   * @return The result of the flow.
   */
  public T getResult() {
    return this.result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<@NonNull T> asOptional() {
    return Optional.ofNullable(this.result);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSuccessful() {
    return true;
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
    SuccessfulResult<?> that = (SuccessfulResult<?>) o;
    return Objects.equals(getResult(), that.getResult());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.getResult());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "SuccessfulResult{" +
        "result=" + this.result +
        '}';
  }
}