package com.proximyst.sewer.piping;

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
   * @throws Exception Any exception this handler may want to throw during its flow.
   */
  Output flow(Input input) throws Exception;
}
