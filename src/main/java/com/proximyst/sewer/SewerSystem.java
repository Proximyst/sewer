package com.proximyst.sewer;

import com.proximyst.sewer.filtration.FiltrationModule;
import com.proximyst.sewer.leakage.SewerException;
import com.proximyst.sewer.piping.PipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.PipeResult.Exceptional;
import com.proximyst.sewer.piping.PipeResult.Success;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A system that takes an {@link Input}, lets it flow through several {@link SewerPipe pipes}, then outputs its {@link
 * PipeResult}, possibly containing an output of the expected {@link Output} type.
 *
 * @param <Input>  The input type to accept.
 * @param <Output> The expected output type.
 * @see #builder(String, PipeHandler)
 * @see #builder(String, PipeHandler, FiltrationModule)
 * @see #builder(String, PipeHandler, FiltrationModule, FiltrationModule)
 */
public class SewerSystem<Input, Output> {
  @NonNull
  private final List<SewerPipe<?, ?>> pipeline;

  @Nullable
  private final Consumer<Exceptional<?>> exceptionHandler;

  private SewerSystem(
      @NonNull List<SewerPipe<?, ?>> pipeline,
      @Nullable Consumer<Exceptional<?>> exceptionHandler
  ) {
    this.pipeline = pipeline;
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * Create a new builder.
   *
   * @param pipeName The name of the initial pipe.
   * @param handler  The handler of the initial pipe.
   * @param <Input>  The input to accept in the initial pipe.
   * @param <Output> The output to expect from the initial pipe.
   * @return A new {@link Builder} to create a new {@link SewerSystem}.
   */
  public static <Input, Output> Builder<Input, Output> builder(
      @NonNull String pipeName,
      @NonNull PipeHandler<Input, Output> handler
  ) {
    return new Builder<>(new SewerPipe<>(pipeName, handler));
  }

  /**
   * Create a new builder.
   *
   * @param pipeName      The name of the initial pipe.
   * @param handler       The handler of the initial pipe.
   * @param preFlowFilter The filter to run before the flow of the initial pipe. May be {@code null}.
   * @param <Input>       The input to accept in the initial pipe.
   * @param <Output>      The output to expect from the initial pipe.
   * @return A new {@link Builder} to create a new {@link SewerSystem}.
   */
  public static <Input, Output> Builder<Input, Output> builder(
      @NonNull String pipeName,
      @NonNull PipeHandler<Input, Output> handler,
      @Nullable FiltrationModule<Input> preFlowFilter
  ) {
    return new Builder<>(new SewerPipe<>(pipeName, handler, preFlowFilter));
  }

  /**
   * Create a new builder.
   *
   * @param pipeName       The name of the initial pipe.
   * @param handler        The handler of the initial pipe.
   * @param preFlowFilter  The filter to run before the flow of the initial pipe. May be {@code null}.
   * @param postFlowFilter The filter to run after the flow of the initial pipe. May be {@code null}.
   * @param <Input>        The input to accept in the initial pipe.
   * @param <Output>       The output to expect from the initial pipe.
   * @return A new {@link Builder} to create a new {@link SewerSystem}.
   */
  public static <Input, Output> Builder<Input, Output> builder(
      @NonNull String pipeName,
      @NonNull PipeHandler<Input, Output> handler,
      @Nullable FiltrationModule<Input> preFlowFilter,
      @Nullable FiltrationModule<Output> postFlowFilter
  ) {
    return new Builder<>(new SewerPipe<>(pipeName, handler, preFlowFilter, postFlowFilter));
  }

  private void handleException(@NonNull Exceptional<?> result) {
    if (exceptionHandler == null) {
      throw new SewerException(result.getPipeName(), result.getException());
    }

    exceptionHandler.accept(result);
  }

  /**
   * Pump an {@link Input} through this system's {@link SewerPipe pipes}.
   *
   * @param input The input to flow through this system.
   * @return A {@link PipeResult} with filters, flow, and exceptions taken into account.
   */
  @SuppressWarnings("unchecked")
  public PipeResult<Output> pump(Input input) {
    PipeResult<?> underways = null;
    for (SewerPipe<?, ?> pipe : pipeline) {
      if (underways == null) {
        underways = ((SewerPipe<Input, ?>) pipe).flow(input);
        continue;
      }

      if (underways.isSuccessful()) {
        underways = ((SewerPipe<Object, ?>) pipe).flow(((Success<Object>) underways).getResult());
      } else if (underways.isExceptional()) {
        handleException((Exceptional<?>) underways);
        return (PipeResult<Output>) underways;
      } else if (underways.isFiltered()) {
        return (PipeResult<Output>) underways;
      } else {
        throw new IllegalStateException("PipeResult type " + underways.getClass().getName() + " is unknown");
      }
    }

    return (PipeResult<Output>) underways;
  }

  /**
   * A builder to create a new {@link SewerSystem}.
   *
   * @param <Input>  The input type of the entire system. This does not change.
   * @param <Output> The output type of the system thus far. This may change with every {@link #pipe(String,
   *                 PipeHandler, FiltrationModule) #pipe} call.
   */
  public static class Builder<Input, Output> {
    @NonNull
    private final List<SewerPipe<?, ?>> pipeline;

    @Nullable
    private Consumer<Exceptional<?>> exceptionHandler;

    @NonNull
    private SewerPipe<?, ?> lastPipe;

    private Builder(@NonNull SewerPipe<Input, Output> handler) {
      this.pipeline = new ArrayList<>();
      this.exceptionHandler = null;

      this.pipeline.add(handler);
      this.lastPipe = handler;
    }

    /**
     * Attach a new pipe to the system.
     *
     * @param pipe        The pipe to attach. This must take an instance of {@link Output} as input.
     * @param <NewOutput> The new output for the system.
     * @return This builder with the new pipe attached.
     */
    @SuppressWarnings("unchecked")
    public <NewOutput> Builder<Input, NewOutput> pipe(@NonNull SewerPipe<Output, NewOutput> pipe) {
      this.pipeline.add(pipe);
      this.lastPipe = pipe;
      return (Builder<Input, NewOutput>) this;
    }

    /**
     * Attach a new pipe to the system.
     *
     * @param pipeName    The name of the pipe to attach.
     * @param handler     The handler of the pipe to attach. This must take an instance of {@link Output} as input.
     * @param <NewOutput> The new output for the system.
     * @return This builder with the new pipe attached.
     */
    public <NewOutput> Builder<Input, NewOutput> pipe(
        @NonNull String pipeName,
        @NonNull PipeHandler<Output, NewOutput> handler
    ) {
      return this.pipe(new SewerPipe<>(pipeName, handler));
    }

    /**
     * Attach a new pipe to the system.
     *
     * @param pipeName      The name of the pipe to attach.
     * @param handler       The handler of the pipe to attach. This must take an instance of {@link Output} as input.
     * @param preFlowFilter The filter to run before the pipe. May be {@code null}.
     * @param <NewOutput>   The new output for the system.
     * @return This builder with the new pipe attached.
     */
    public <NewOutput> Builder<Input, NewOutput> pipe(
        @NonNull String pipeName,
        @NonNull PipeHandler<Output, NewOutput> handler,
        @Nullable FiltrationModule<Output> preFlowFilter
    ) {
      return this.pipe(new SewerPipe<>(pipeName, handler, preFlowFilter));
    }

    /**
     * Attach a new pipe to the system.
     *
     * @param pipeName       The name of the pipe to attach.
     * @param handler        The handler of the pipe to attach. This must take an instance of {@link Output} as input.
     * @param preFlowFilter  The filter to run before the pipe. May be {@code null}.
     * @param postFlowFilter The filter to run after the pipe. May be {@code null}.
     * @param <NewOutput>    The new output for the system.
     * @return This builder with the new pipe attached.
     */
    public <NewOutput> Builder<Input, NewOutput> pipe(
        @NonNull String pipeName,
        @NonNull PipeHandler<Output, NewOutput> handler,
        @Nullable FiltrationModule<Output> preFlowFilter,
        @Nullable FiltrationModule<NewOutput> postFlowFilter
    ) {
      return this.pipe(new SewerPipe<>(pipeName, handler, preFlowFilter, postFlowFilter));
    }

    /**
     * Set the exception handler for the system.
     * <p/>
     * This does not delegate to the old handler.
     *
     * @param consumer The new exception handler.
     * @return This builder with the new exception handler attached.
     */
    public Builder<Input, Output> exceptionHandler(@NonNull Consumer<Exceptional<?>> consumer) {
      this.exceptionHandler = consumer;
      return this;
    }

    /**
     * Build the {@link SewerSystem} with the pipes attached.
     * <p/>
     * This clones the pipeline, and the builder may therefore be used again after the call.
     *
     * @return A new {@link SewerSystem} akin this builder.
     */
    public SewerSystem<Input, Output> build() {
      return new SewerSystem<>(
          Collections.unmodifiableList(new ArrayList<>(this.pipeline)),
          this.exceptionHandler
      );
    }
  }
}
