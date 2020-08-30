package com.proximyst.sewer.filtration;

import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A {@link FiltrationModule} for any {@code Optional<T>} ensuring the flow is non-null.
 *
 * @param <T> The type to ensure the type of.
 */
public final class PresentOptionalFiltrationModule<T> implements FiltrationModule<@Nullable Optional<@Nullable T>> {
  private static final PresentOptionalFiltrationModule<?> INSTANCE = new PresentOptionalFiltrationModule<>();

  private PresentOptionalFiltrationModule() {
  }

  /**
   * Get an instance of {@link PresentOptionalFiltrationModule} for {@link Optional} presence for a given type {@link
   * T}.
   *
   * @param <T> The type to check the presence of.
   * @return A filter for {@link Optional} presence for a given type {@link T}.
   */
  @SuppressWarnings("unchecked") // We need not care as it doesn't use the type in any way.
  @NonNull
  @Pure
  public static <@Nullable T> PresentOptionalFiltrationModule<@Nullable T> getInstance() {
    return (PresentOptionalFiltrationModule<T>) INSTANCE;
  }

  @Override
  public boolean allowFlow(@Nullable Optional<@Nullable T> t) {
    return t != null && t.isPresent();
  }

  @Override
  public String toString() {
    return "PresentOptionalFiltrationModule{}";
  }
}
