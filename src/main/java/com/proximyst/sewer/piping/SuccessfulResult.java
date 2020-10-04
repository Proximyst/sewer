package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class SuccessfulResult<T> extends PipeResult<T> {
  private final T result;

  public SuccessfulResult(T result) {
    this.result = result;
  }

  public T getResult() {
    return this.result;
  }

  @Override
  public @NonNull Optional<@NonNull T> asOptional() {
    return Optional.ofNullable(this.result);
  }

  @Override
  public boolean isSuccessful() {
    return true;
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(this.getResult());
  }

  @Override
  public String toString() {
    return "SuccessfulResult{" +
        "result=" + this.result +
        '}';
  }
}