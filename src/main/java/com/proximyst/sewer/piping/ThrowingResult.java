package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ThrowingResult<Ty> extends PipeResult<Ty> {
  private final @NonNull Throwable throwable;

  public ThrowingResult(@NonNull Throwable throwable) {
    this.throwable = throwable;
  }

  public @NonNull Throwable getThrowable() {
    return this.throwable;
  }

  @Override
  public boolean isSuccessful() {
    return false;
  }

  @Override
  public @NonNull Optional<@NonNull Ty> asOptional() {
    return Optional.empty();
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(this.getThrowable());
  }

  @Override
  public String toString() {
    return "ThrowableResult{" +
        "throwable=" + this.throwable +
        '}';
  }
}
