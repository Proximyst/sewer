package com.proximyst.sewer.piping;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

public final class NamedPipeResult<T, PR extends PipeResult<T>> extends PipeResult<T> {
  private final @NonNull String pipeName;
  private final @NonNull PR result;

  public NamedPipeResult(@NonNull String pipeName, @NonNull PR result) {
    this.pipeName = pipeName;
    this.result = result;
  }

  @Override
  @Pure
  public boolean isSuccessful() {
    return this.result.isSuccessful();
  }

  @Override
  @Pure
  public boolean mayContinue() {
    return this.result.mayContinue();
  }

  @Override
  @SideEffectFree
  public @NonNull Optional<@NonNull T> asOptional() {
    return this.result.asOptional();
  }

  @Pure
  public @NonNull PR getResult() {
    return this.result;
  }

  @Pure
  public @NonNull String getPipeName() {
    return this.pipeName;
  }
}
