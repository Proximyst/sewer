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
public interface PipeHandler<Input, Output> {
  /**
   * Flow the input through this handler for an output.
   *
   * @param input The input expected to flow through this handler to produce an output.
   * @return The output created by this handler.
   */
  @NonNull
  CompletableFuture<Output> flow(Input input);
}
