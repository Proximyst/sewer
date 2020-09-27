package com.proximyst.sewer.filtration;

import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A module to allow filtering of flow.
 *
 * @param <Input> The input to expect in this filtration module.
 */
@FunctionalInterface
public interface ImmediateFiltrationModule<Input> extends FiltrationModule<Input> {
  static <Input> ImmediateFiltrationModule<Input> of(@NonNull ImmediateFiltrationModule<Input> module) {
    return module;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NonNull
  default CompletableFuture<@NonNull Boolean> allowFlow(Input input) {
    return CompletableFuture.completedFuture(allowFlowImmediately(input));
  }

  /**
   * Whether the pipe this is attached to will allow flow of {@link Input}.
   *
   * @param input The input to check the flow of.
   * @return Whether the flow shall be allowed.
   */
  boolean allowFlowImmediately(Input input);
}
