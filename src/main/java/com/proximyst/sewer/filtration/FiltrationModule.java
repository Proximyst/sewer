package com.proximyst.sewer.filtration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A module to allow filtering of flow.
 *
 * @param <Input> The input to expect in this filtration module.
 */
@FunctionalInterface
public interface FiltrationModule<Input> {
  /**
   * Whether the pipe this is attached to will allow flow of {@link Input}.
   *
   * @param input The input to check the flow of.
   * @return Whether the flow shall be allowed.
   */
  boolean allowFlow(Input input);

  /**
   * Compose this filtration module with another with a boolean AND.
   *
   * @param module The module to compose with.
   * @return A new module composing this and the given module.
   */
  @NonNull
  @SideEffectFree
  default FiltrationModule<Input> and(@NonNull FiltrationModule<Input> module) {
    return input -> this.allowFlow(input) && module.allowFlow(input);
  }

  /**
   * Compose this filtration module with another with a boolean OR.
   *
   * @param module The module to compose with.
   * @return A new module composing this and the given module.
   */
  @NonNull
  @SideEffectFree
  default FiltrationModule<Input> or(@NonNull FiltrationModule<Input> module) {
    return input -> this.allowFlow(input) || module.allowFlow(input);
  }

  /**
   * Compose this filtration module with another with a boolean XOR.
   *
   * @param module The module to compose with.
   * @return A new module composing this and the given module.
   */
  @NonNull
  @SideEffectFree
  default FiltrationModule<Input> xor(@NonNull FiltrationModule<Input> module) {
    return input -> this.allowFlow(input) ^ module.allowFlow(input);
  }

  /**
   * Negate this filtration module.
   *
   * @return This filtration module where {@link #allowFlow} is negated.
   */
  @NonNull
  @SideEffectFree
  default FiltrationModule<Input> not() {
    return input -> !this.allowFlow(input);
  }
}
