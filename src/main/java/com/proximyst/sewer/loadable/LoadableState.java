package com.proximyst.sewer.loadable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Internal state for {@link Loadable}s.
 */
class LoadableState {
  /**
   * A value that has not yet been loaded.
   */
  public static class Unloaded extends LoadableState {
    /**
     * The single instance of this state.
     * <p>
     * The state carries no actual values, so no further instances will be necessary.
     */
    private static final Unloaded INSTANCE = new Unloaded();

    private Unloaded() {
    }

    /**
     * Get an instance of {@link Unloaded}.
     *
     * @return An unloaded {@link LoadableState} instance.
     */
    @Pure
    public static @NonNull Unloaded getInstance() {
      return INSTANCE;
    }
  }

  /**
   * A value that has been loaded.
   *
   * @param <T> The type of the value contained.
   */
  public static class Loaded<T> extends LoadableState {
    /**
     * The loaded item.
     */
    private final T item;

    public Loaded(T item) {
      this.item = item;
    }

    /**
     * Fetch the internal item of type {@link T}.
     *
     * @return The internal item.
     */
    public T getItem() {
      return item;
    }
  }
}
