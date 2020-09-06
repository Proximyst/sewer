package com.proximyst.sewer.loadable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Internal state for {@link Loadable}s.
 *
 * @param <T> The type of the value contained.
 */
class LoadableState<@NonNull T> {
  /**
   * A value that has not yet been loaded.
   *
   * @param <T> The type of the value contained.
   */
  public static class Unloaded<@NonNull T> extends LoadableState<T> {
    /**
     * The single instance of this state.
     * <p>
     * The state carries no actual values, so no further instances will be necessary.
     */
    private static final Unloaded<?> INSTANCE = new Unloaded<>();

    private Unloaded() {
    }

    /**
     * Get an instance of {@link Unloaded} for a given {@link T}.
     *
     * @param <T> The type to store represent this state.
     * @return An unloaded {@link LoadableState} instance.
     */
    @SuppressWarnings("unchecked") // Only one instance is needed, and the generic type does not matter.
    @NonNull
    @Pure
    public static <T> Unloaded<T> getInstance() {
      return (Unloaded<T>) INSTANCE;
    }
  }

  /**
   * A value that has been loaded.
   *
   * @param <T> The type of the value contained.
   */
  public static class Loaded<@NonNull T> extends LoadableState<T> {
    /**
     * The loaded item.
     */
    @NonNull
    private final T item;

    public Loaded(@NonNull T item) {
      this.item = item;
    }

    /**
     * Fetch the internal item of type {@link T}.
     *
     * @return The internal item.
     */
    @NonNull
    public T getItem() {
      return item;
    }
  }
}
