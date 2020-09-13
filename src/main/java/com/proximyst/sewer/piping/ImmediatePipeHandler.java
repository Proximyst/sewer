package com.proximyst.sewer.piping;

import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The handler of a pipe.
 *
 * @param <Input>  The input this handler may accept.
 * @param <Output> The output this handler may accept.
 */
@FunctionalInterface
public interface ImmediatePipeHandler<Input, Output> extends PipeHandler<Input, Output> {
  static <Input, Output> ImmediatePipeHandler<Input, Output> of(@NonNull ImmediatePipeHandler<Input, Output> handler) {
    return handler;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NonNull
  default CompletableFuture<Output> flow(Input input) {
    try {
      return CompletableFuture.completedFuture(flowImmediately(input));
    } catch (Exception ex) {
      CompletableFuture<Output> future = new CompletableFuture<>();
      future.completeExceptionally(ex);
      return future;
    }
  }

  /**
   * Flow the input through this handler for an output.
   *
   * @param input The input expected to flow through this handler to produce an output.
   * @return The output created by this handler.
   */
  Output flowImmediately(Input input) throws Exception;
}
