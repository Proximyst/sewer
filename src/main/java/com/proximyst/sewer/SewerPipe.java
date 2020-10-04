package com.proximyst.sewer;

import com.proximyst.sewer.piping.NamedPipeResult;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.ThrowingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.common.value.qual.MinLen;

/**
 * A pipe with an expected {@link Input} and {@link Output}.
 *
 * @param <Input>  The input type to give the {@link PipeHandler}.
 * @param <Output> The type returned by the {@link PipeHandler}.
 */
public final class SewerPipe<Input, Output> {
  private final @NonNull String pipeName;
  private final @NonNull Module<?, ?> @NonNull @MinLen(1) [] modules;

  SewerPipe(@NonNull String pipeName, @NonNull Module<Input, Output> module) {
    this(pipeName, new Module<?, ?>[]{module});
  }

  SewerPipe(
      @NonNull String pipeName,
      @NonNull Module<?, ?> @NonNull @MinLen(1) [] modules
  ) {
    this.pipeName = pipeName;
    this.modules = modules;
  }

  public static <Input, Output> @NonNull Builder<Input, Output> builder(
      @NonNull @MinLen(1) String pipeName,
      @NonNull Module<Input, Output> module
  ) {
    return new Builder<>(pipeName, module);
  }

  /**
   * @return The identifying name of this pipe.
   */
  public @NonNull String getPipeName() {
    return this.pipeName;
  }

  /**
   * Flow an {@link Input} through this pipe's {@link PipeHandler}.
   *
   * @param input The input to flow through.
   * @return A {@link CompletableFuture future-wrapped} {@link PipeResult} of the {@link Output}.
   */
  @SuppressWarnings("unchecked") // Required; we're trusting the constructors were called type-checked
  public @NonNull CompletableFuture<@NonNull NamedPipeResult<Output, ? extends PipeResult<Output>>> flow(Input input) {
    CompletableFuture<NamedPipeResult<?, ? extends PipeResult<?>>> future = null;
    for (Module<?, ?> module : this.modules) {
      if (future == null) {
        future = ((Module<Input, ? extends PipeResult<?>>) module).flow(input)
            .thenApply(output -> new NamedPipeResult<>(this.getPipeName(), output));
        continue;
      }

      final CompletableFuture<NamedPipeResult<?, ? extends PipeResult<?>>> preModificationFuture = future;
      future = future.thenCompose(res -> {
        if (!res.mayContinue()) {
          return preModificationFuture;
        }

        return ((Module<Object, ?>) module).flow(res.asOptional().orElse(null))
            .thenApply(output -> new NamedPipeResult<>(this.getPipeName(), output));
      });
    }

    // #requireNonNull because there is always at least 1 module.
    return (CompletableFuture<NamedPipeResult<Output, ? extends PipeResult<Output>>>) (CompletableFuture<?>)
        Objects.requireNonNull(future)
            .exceptionally(throwable ->
                new NamedPipeResult<>(this.getPipeName(), new ThrowingResult<Output>(throwable)));
  }

  @SuppressWarnings("unchecked") // Magic casts required to be type-safe for the user.
  public static class Builder<Input, Output> {
    private final @NonNull String name;
    private final @NonNull @MinLen(1) List<@NonNull Module<?, ?>> modules;

    private Builder(
        @NonNull String name,
        @NonNull Module<Input, Output> module
    ) {
      this.name = name;
      this.modules = new ArrayList<>();
      this.modules.add(module);
    }

    public <NewOutput> @NonNull @This Builder<Input, NewOutput> pipe(@NonNull Module<Output, NewOutput> module) {
      this.modules.add(module);
      return (Builder<Input, NewOutput>) this;
    }

    public @NonNull SewerPipe<Input, Output> build() {
      return new SewerPipe<>(name, modules.toArray(new Module<?, ?>[0]));
    }
  }
}
