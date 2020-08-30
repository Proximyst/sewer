package com.proximyst.sewer.filtration;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A {@link FiltrationModule} for any {@code T} ensuring the flow is non-null.
 *
 * @param <T> The type to ensure the type of.
 */
public final class NonNullFiltrationModule<T> implements FiltrationModule<@Nullable T> {
  private static final NonNullFiltrationModule<?> INSTANCE = new NonNullFiltrationModule<>();

  private NonNullFiltrationModule() {
  }

  /**
   * Get an instance of {@link NonNullFiltrationModule} for a given type {@link T}.
   *
   * @param <T> The type to check nullability of.
   * @return A filter for nullability for a given type {@link T}.
   */
  @SuppressWarnings("unchecked") // We need not care as it doesn't use the type in any way.
  @NonNull
  @Pure
  public static <@Nullable T> NonNullFiltrationModule<@Nullable T> getInstance() {
    return (NonNullFiltrationModule<T>) INSTANCE;
  }

  @Override
  public boolean allowFlow(@Nullable T t) {
    return t != null;
  }

  @Override
  public String toString() {
    return "NonNullFiltrationModule{}";
  }
}
