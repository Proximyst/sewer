package com.proximyst.sewer;

import com.proximyst.sewer.filtration.FiltrationModule;
import com.proximyst.sewer.piping.PipeResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link SewerPipe} for piping {@link SewerSystem}s.
 *
 * @param <Input>  The input type to give the {@link SewerSystem}.
 * @param <Output> The type returned by the {@link SewerSystem}.
 */
class SystemPipe<Input, Output> extends SewerPipe<Input, PipeResult<Output>> {
  public SystemPipe(
      @NonNull String pipeName,
      @Nullable FiltrationModule<Input> preFlowFilter,
      @Nullable FiltrationModule<Output> postFlowFilter,
      @NonNull SewerSystem<Input, Output> pipeline
  ) {
    super(
        pipeName,
        pipeline::pump,
        preFlowFilter,
        res -> postFlowFilter == null || postFlowFilter.allowFlow(res.asSuccess().getResult())
    );
  }

  @Override
  @NonNull
  public PipeResult<PipeResult<Output>> flow(Input input) {
    // Let it first flow through the pipe before we start touching it.
    PipeResult<PipeResult<Output>> superResult = super.flow(input);

    if (!superResult.isSuccessful()) {
      // Something happened outside the inner pipeline.
      return superResult;
    }

    PipeResult<Output> result = superResult.asSuccess().getResult();

    if (result.isSuccessful()) {
      return superResult;
    }

    if (result.isExceptional()) {
      return PipeResult
          .exceptional(this.getPipeName() + ": " + result.getPipeName(), result.asExceptional().getException());
    }

    if (result.isFiltered()) {
      return result instanceof PipeResult.PreFilter
          ? PipeResult.beforeFilter(this.getPipeName() + ": " + result.getPipeName())
          : PipeResult.postFilter(this.getPipeName() + ": " + result.getPipeName(), result);
    }

    throw new IllegalStateException("PipeResult was not recognised.");
  }
}
