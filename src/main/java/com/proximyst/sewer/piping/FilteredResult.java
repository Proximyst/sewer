package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FilteredResult<T> extends PipeResult<T> {
  @Override
  public boolean isSuccessful() {
    return false;
  }

  @Override
  public @NonNull Optional<T> asOptional() {
    return Optional.empty();
  }

  @Override
  public boolean mayContinue() {
    return false;
  }
}
