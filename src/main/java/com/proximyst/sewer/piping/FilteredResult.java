package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A result which has been filtered.
 *
 * @param <T> The output type.
 * @since 0.7.0
 */
public final class FilteredResult<T> extends PipeResult<T> {
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
  public @NonNull Optional<T> asOptional() {
    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean mayContinue() {
    return false;
  }

  @Override
  public String toString() {
    return "FilteredResult{}";
  }
}
