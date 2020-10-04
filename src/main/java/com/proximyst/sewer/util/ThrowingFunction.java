package com.proximyst.sewer.util;

import java.util.Objects;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * @param <In>  The type of the input to the function.
 * @param <Out> The type of the result of the function.
 * @param <Thr> The type of the throwable of the function.
 */
@FunctionalInterface
public interface ThrowingFunction<In, Out, Thr extends Throwable> {
  /**
   * Applies this function to the given argument.
   *
   * @param input The function argument.
   * @return The function result.
   * @throws Thr The function throwable thrown.
   */
  Out apply(In input) throws Thr;

  /**
   * Returns a composed function that first applies the {@code before} function to its input, and then applies this
   * function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
   * composed function.
   *
   * @param <V>    The type of input to the {@code before} function, and to the composed function.
   * @param before The function to apply before this function is applied.
   * @return A composed function that first applies the {@code before} function and then applies this function.
   * @throws NullPointerException If before is null.
   * @see #andThen(ThrowingFunction)
   */
  default <V> ThrowingFunction<V, Out, Thr> compose(ThrowingFunction<? super V, ? extends In, Thr> before) {
    Objects.requireNonNull(before);
    return (V v) -> apply(before.apply(v));
  }

  /**
   * Returns a composed function that first applies this function to its input, and then applies the {@code after}
   * function to the result. If evaluation of either function throws an exception, it is relayed to the caller of the
   * composed function.
   *
   * @param <V>   The type of output of the {@code after} function, and of the composed function.
   * @param after The function to apply after this function is applied.
   * @return A composed function that first applies this function and then applies the {@code after} function.
   * @throws NullPointerException If after is null.
   * @see #compose(ThrowingFunction)
   */
  default <V> ThrowingFunction<In, V, Thr> andThen(ThrowingFunction<? super Out, ? extends V, Thr> after) {
    Objects.requireNonNull(after);
    return (In t) -> after.apply(apply(t));
  }

  /**
   * Returns a function that always returns its input argument.
   *
   * @param <T> The type of the input and output objects to the function.
   * @return A function that always returns its input argument.
   */
  static <T, Thr extends Throwable> ThrowingFunction<T, T, Thr> identity() {
    return t -> t;
  }
}
