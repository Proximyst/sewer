package com.proximyst.sewer.piping;

import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ImmediatePipeHandler<Input, Output> extends PipeHandler<Input, Output> {
  static <Input, Output> ImmediatePipeHandler<Input, Output> of(@NonNull ImmediatePipeHandler<Input, Output> handler) {
    return handler;
  }

  @Override
  default CompletableFuture<Output> flow(Input input) {
    try {
      return CompletableFuture.completedFuture(flowImmediately(input));
    } catch (Exception ex) {
      CompletableFuture<Output> future = new CompletableFuture<>();
      future.completeExceptionally(ex);
      return future;
    }
  }

  Output flowImmediately(Input input) throws Exception;
}
