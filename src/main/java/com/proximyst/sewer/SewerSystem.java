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
 */
public class SewerSystem<Input, Output> {
  private final @NonNull SewerPipe<?, ?> @NonNull @MinLen(1) [] pipeline;

  private SewerSystem(@NonNull SewerPipe<?, ?> @NonNull @MinLen(1) [] pipeline) {
    this.pipeline = pipeline;
  }

  public static <Input, Output> @NonNull Builder<Input, Output> builder(
      @NonNull SewerPipe<Input, Output> module
  ) {
    return new Builder<>(module);
  }

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
   * @return TODO
   */
  @SuppressWarnings("unchecked")
  public @NonNull CompletableFuture<@NonNull NamedPipeResult<Output, ? extends PipeResult<Output>>> pump(
      final Input input) {
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

  @SuppressWarnings("unchecked") // Magic casts required to be type-safe for the user.
  public static class Builder<Input, Output> {
    private final @NonNull @MinLen(1) List<@NonNull SewerPipe<?, ?>> pipes;

    private Builder(
        @NonNull SewerPipe<Input, Output> pipe
    ) {
      this.pipes = new ArrayList<>();
      this.pipes.add(pipe);
    }

    public <NewOutput> @NonNull @This Builder<Input, NewOutput> pipe(@NonNull SewerPipe<Output, NewOutput> pipe) {
      this.pipes.add(pipe);
      return (Builder<Input, NewOutput>) this;
    }

    public <NewOutput> @NonNull @This Builder<Input, NewOutput> module(@NonNull String name,
        @NonNull Module<Output, NewOutput> module) {
      return this.pipe(new SewerPipe<>(name, module));
    }

    public @NonNull SewerSystem<Input, Output> build() {
      return new SewerSystem<>(pipes.toArray(new SewerPipe[0]));
    }
  }
}
