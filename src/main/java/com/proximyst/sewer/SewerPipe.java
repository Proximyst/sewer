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
 * @param <Input>  The input type of this pipe.
 * @param <Output> The type returned by this pipe after all the {@link Module modules} have been flowed through.
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

  /**
   * Create a new builder to build an instance of {@link SewerPipe}, taking an {@link Input} to return an {@link
   * Output}.
   *
   * @param pipeName The name of the pipe.
   * @param module   The single module this pipe shall consist of to begin with.
   * @param <Input>  The input type for the pipe.
   * @param <Output> The output type for the pipe.
   * @return A new {@link Builder} to create a new {@link SewerPipe}.
   */
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
   * Flow an {@link Input} through this pipe's {@link Module modules}.
   *
   * @param input The input to flow through.
   * @return A {@link CompletableFuture future-wrapped} {@link NamedPipeResult} of an {@link Output}. Be aware that this
   * is a <i>wrapper</i>, and is not an instance of {@link ThrowingResult} if some {@link Module} throws.
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

  /**
   * A builder to create a new {@link SewerPipe} which accepts an {@link Input} and returns an {@link Output}.
   *
   * @param <Input>  The type to accept as input.
   * @param <Output> The type to accept as output.
   */
  @SuppressWarnings("unchecked") // Magic casts required to be type-safe for the user.
  public static class Builder<Input, Output> {
    private final @NonNull String name;

    /**
     * The internal modules in this pipe.
     * <p>
     * There must be at least 1 module per pipe.
     */
    private final @NonNull @MinLen(1) List<@NonNull Module<?, ?>> modules;

    private Builder(
        @NonNull String name,
        @NonNull Module<Input, Output> module
    ) {
      this.name = name;
      this.modules = new ArrayList<>();
      this.modules.add(module);
    }

    /**
     * Add a module to the pipe, and migrate the output type to its output type.
     *
     * @param module      The new module to add to the pipe.
     * @param <NewOutput> The new output type of the {@link SewerPipe} this will {@link #build() build}.
     * @return This builder for chaining.
     */
    public <NewOutput> @NonNull @This Builder<Input, NewOutput> pipe(@NonNull Module<Output, NewOutput> module) {
      this.modules.add(module);
      return (Builder<Input, NewOutput>) this;
    }

    /**
     * Build a new {@link SewerPipe}, taking an {@link Input} in exchange for an {@link Output}.
     *
     * @return A new {@link SewerPipe} with the modules added through this builder.
     */
    public @NonNull SewerPipe<Input, Output> build() {
      return new SewerPipe<>(name, modules.toArray(new Module<?, ?>[0]));
    }
  }
}
