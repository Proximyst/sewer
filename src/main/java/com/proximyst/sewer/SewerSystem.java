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
 * A system that takes an {@link Input}, lets it flow through several {@link SewerPipe pipes}, then outputs its {@link
 * PipeResult}, possibly containing an output of the expected {@link Output} type.
 *
 * @param <Input>  The input type to accept.
 * @param <Output> The expected output type.
 * @since 0.1.0
 */
public class SewerSystem<Input, Output> {
  /**
   * All the pipes in this system.
   *
   * @since 0.7.0
   */
  private final @NonNull SewerPipe<?, ?> @NonNull @MinLen(1) [] pipeline;

  /**
   * @param pipeline The pipes to use in this system.
   * @since 0.7.0
   */
  private SewerSystem(@NonNull SewerPipe<?, ?> @NonNull @MinLen(1) [] pipeline) {
    this.pipeline = pipeline;
  }

  /**
   * Create a new builder to build an instance of {@link SewerSystem}, taking an {@link Input} to return an {@link
   * Output}.
   *
   * @param pipe     The pipe to add as the first pipe of the system.
   * @param <Input>  The input type for the first pipe.
   * @param <Output> The output type for the first pipe.
   * @return A new {@link Builder} to create a new {@link SewerSystem}.
   * @since 0.7.0
   */
  public static <Input, Output> @NonNull Builder<Input, Output> builder(
      @NonNull SewerPipe<Input, Output> pipe
  ) {
    return new Builder<>(pipe);
  }

  /**
   * Create a new builder to build an instance of {@link SewerSystem}, taking an {@link Input} to return an {@link
   * Output}.
   *
   * @param pipeName The name of the first pipe to add to the system.
   * @param module   The single module the first pipe of the system shall consist of.
   * @param <Input>  The input type for the first pipe.
   * @param <Output> The output type for the first pipe.
   * @return A new {@link Builder} to create a new {@link SewerSystem}.
   * @since 0.7.0
   */
  public static <Input, Output> @NonNull Builder<Input, Output> builder(
      @NonNull @MinLen(1) String pipeName,
      @NonNull Module<Input, Output> module
  ) {
    return SewerSystem.builder(SewerPipe.builder(pipeName, module).build());
  }

  /**
   * Pump an {@link Input} through this system's {@link SewerPipe pipes}.
   *
   * @param input The input to flow through this system.
   * @return A {@link CompletableFuture future-wrapped} {@link NamedPipeResult} of an {@link Output}. Be aware that this
   * is a <i>wrapper</i>, and is not an instance of {@link ThrowingResult} if some {@link SewerPipe} throws.
   * @since 0.7.0
   */
  @SuppressWarnings("unchecked")
  public @NonNull CompletableFuture<@NonNull NamedPipeResult<Output, ? extends PipeResult<Output>>> pump(
      final Input input
  ) {
    CompletableFuture<NamedPipeResult<?, ? extends PipeResult<?>>> underways = null;
    for (SewerPipe<?, ?> pipe : pipeline) {
      if (underways == null) {
        try {
          underways = (CompletableFuture<NamedPipeResult<?, ? extends PipeResult<?>>>) (CompletableFuture<?>)
              ((SewerPipe<Input, ?>) pipe).flow(input);
        } catch (final Throwable ex) {
          underways = CompletableFuture
              .completedFuture(new NamedPipeResult<>(pipe.getPipeName(), new ThrowingResult<>(ex)));
        }
        continue;
      }

      final CompletableFuture<NamedPipeResult<?, ? extends PipeResult<?>>> preModificationFuture = underways;
      underways = underways.thenCompose(res -> {
        if (!res.mayContinue()) {
          return preModificationFuture;
        }

        return ((SewerPipe<Object, ?>) pipe).flow(res.asOptional().orElse(null))
            .thenApply(output -> new NamedPipeResult<>(pipe.getPipeName(), output));
      });
    }

    // #requireNonNull because there is always at least 1 pipe.
    return (CompletableFuture<NamedPipeResult<Output, ? extends PipeResult<Output>>>) (CompletableFuture<?>)
        Objects.requireNonNull(underways);
  }

  /**
   * A builder to create a new {@link SewerSystem} which accepts an {@link Input} and returns an {@link Output}.
   *
   * @param <Input>  The type to accept as input.
   * @param <Output> The type to accept as output.
   */
  @SuppressWarnings("unchecked") // Magic casts required to be type-safe for the user.
  public static class Builder<Input, Output> {
    /**
     * The internal pipes in this system.
     * <p>
     * There must be at least 1 pipe per system.
     *
     * @since 0.7.0
     */
    private final @NonNull @MinLen(1) List<@NonNull SewerPipe<?, ?>> pipes;

    /**
     * @param pipe The first pipe to add in this system.
     */
    private Builder(@NonNull SewerPipe<Input, Output> pipe) {
      this.pipes = new ArrayList<>();
      this.pipes.add(pipe);
    }

    /**
     * Add a pipe to the system, and migrate the output type to its output type.
     *
     * @param pipe        The pipe to add.
     * @param <NewOutput> The new output type of the {@link SewerSystem} this will {@link #build() build}.
     * @return This builder for chaining.
     */
    public <NewOutput> @NonNull @This Builder<Input, NewOutput> pipe(@NonNull SewerPipe<Output, NewOutput> pipe) {
      this.pipes.add(pipe);
      return (Builder<Input, NewOutput>) this;
    }

    /**
     * Add a pipe to the system, and migrate the output type to its output type.
     *
     * @param name        The name of the new pipe to add.
     * @param module      The single module the pipe shall consist of.
     * @param <NewOutput> The new output type of the {@link SewerSystem} this will {@link #build() build}.
     * @return This builder for chaining.
     * @see SewerPipe#builder(String, Module)
     * @since 0.7.0
     */
    public <NewOutput> @NonNull @This Builder<Input, NewOutput> module(
        @NonNull String name,
        @NonNull Module<Output, NewOutput> module
    ) {
      return this.pipe(new SewerPipe<>(name, module));
    }

    /**
     * Build a new {@link SewerSystem}, taking an {@link Input} in exchange for an {@link Output}.
     *
     * @return A new {@link SewerSystem} with the pipes added through this builder.
     */
    public @NonNull SewerSystem<Input, Output> build() {
      return new SewerSystem<>(pipes.toArray(new SewerPipe[0]));
    }
  }
}
