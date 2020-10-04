package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A {@link PipeResult} originating from a flow which threw a {@link Throwable}.
 *
 * @param <Ty> The output type.
 * @since 0.7.0
 */
public final class ThrowingResult<Ty> extends PipeResult<Ty> {
  private final @NonNull Throwable throwable;

  /**
   * @param throwable The {@link Throwable} this result represents.
   */
  public ThrowingResult(@NonNull Throwable throwable) {
    this.throwable = throwable;
  }

  /**
   * @return The thrown {@link Throwable} in the flow.
   */
  public @NonNull Throwable getThrowable() {
    return this.throwable;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSuccessful() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<@NonNull Ty> asOptional() {
    return Optional.empty();
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
    ThrowingResult<?> that = (ThrowingResult<?>) o;
    return getThrowable().equals(that.getThrowable());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.getThrowable());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "ThrowableResult{" +
        "throwable=" + this.throwable +
        '}';
  }
}
