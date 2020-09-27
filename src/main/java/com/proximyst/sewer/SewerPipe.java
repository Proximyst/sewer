package com.proximyst.sewer;

import com.proximyst.sewer.filtration.FiltrationModule;
import com.proximyst.sewer.piping.PipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A pipe with an expected {@link Input} and {@link Output}, potentially with {@link FiltrationModule}s.
 *
 * @param <Input>  The input type to give the {@link PipeHandler}.
 * @param <Output> The type returned by the {@link PipeHandler}.
 */
public class SewerPipe<Input, Output> {
  @NonNull
  private final String pipeName;

  @NonNull
  private final PipeHandler<Input, Output> handler;

  @Nullable
  private final FiltrationModule<Input> preFlowFilter;

  @Nullable
  private final FiltrationModule<Output> postFlowFilter;

  public SewerPipe(@NonNull String pipeName, @NonNull PipeHandler<Input, Output> handler) {
    this(pipeName, handler, null, null);
  }

  public SewerPipe(
      @NonNull String pipeName,
      @NonNull PipeHandler<Input, Output> handler,
      @Nullable FiltrationModule<Input> preFlowFilter
  ) {
    this(pipeName, handler, preFlowFilter, null);
  }

  public SewerPipe(
      @NonNull String pipeName,
      @NonNull PipeHandler<Input, Output> handler,
      @Nullable FiltrationModule<Input> preFlowFilter,
      @Nullable FiltrationModule<Output> postFlowFilter
  ) {
    this.pipeName = pipeName;
    this.handler = handler;
    this.preFlowFilter = preFlowFilter;
    this.postFlowFilter = postFlowFilter;
  }

  /**
   * @return The identifying name of this pipe.
   */
  @NonNull
  public String getPipeName() {
    return pipeName;
  }

  /**
   * Flow an {@link Input} through this pipe's {@link PipeHandler}.
   *
   * @param input The input to flow through.
   * @return A {@link PipeResult} with filters, flow, and exceptions taken into account.
   */
  @NonNull
  public CompletableFuture<PipeResult<Output>> flow(Input input) {
    return (preFlowFilter != null ? preFlowFilter.allowFlow(input) : CompletableFuture.completedFuture(true))
        .<PipeResult<Output>>thenCompose(allow -> {
          if (!allow) {
            return CompletableFuture.completedFuture(PipeResult.beforeFilter(getPipeName()));
          }

          return handler.flow(input).thenApply(result -> PipeResult.success(getPipeName(), result));
        })
        .thenCompose(result -> {
          if (postFlowFilter != null && result.isSuccessful()) {
            return postFlowFilter.allowFlow(result.asSuccess().getResult())
                .thenApply(allow -> {
                  if (allow) {
                    return result;
                  }

                  return PipeResult.postFilter(getPipeName(), result.asSuccess().getResult());
                });
          }

          return CompletableFuture.completedFuture(result);
        })
        .exceptionally(throwable -> PipeResult.exceptional(getPipeName(), throwable));
  }
}
