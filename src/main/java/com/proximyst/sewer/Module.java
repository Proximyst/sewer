package com.proximyst.sewer;

import com.proximyst.sewer.piping.FilteredResult;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.SuccessfulResult;
import com.proximyst.sewer.util.SewerInternalUtilSneakyThrow;
import com.proximyst.sewer.util.ThrowingFunction;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A module for a pipe.
 *
 * @param <Input>  The type to accept when flowing through the module.
 * @param <Output> The type the module shall output.
 * @since 0.7.0
 */
@FunctionalInterface
public interface Module<Input, Output> {
  /**
   * Create a new {@link Module} that returns an {@link Output} immediately.
   *
   * @param function The function to apply to the {@link Input}. This may throw.
   * @param <Input>  The input type to accept.
   * @param <Output> The output type of the {@link ThrowingFunction}.
   * @return A new {@link Module} mapping its {@link Input} through a {@link ThrowingFunction}.
   */
  @SuppressWarnings("deprecation") // Internal class warning.
  static <Input, Output> @NonNull Module<Input, Output> immediately(
      @NonNull ThrowingFunction<Input, @NonNull PipeResult<Output>, ?> function
  ) {
    return in -> {
      try {
        return CompletableFuture.completedFuture(function.apply(in));
      } catch (Throwable throwable) {
        SewerInternalUtilSneakyThrow.sneakyThrow(throwable);
        throw new RuntimeException();
      }
    };
  }

  /**
   * Create a new {@link Module} that returns a wrapped {@link Output} immediately.
   *
   * @param function The function to apply to the {@link Input}. This may throw.
   * @param <Input>  The input type to accept.
   * @param <Output> The output type of the {@link ThrowingFunction}.
   * @return A new {@link Module} mapping its {@link Input} through a {@link ThrowingFunction}.
   */
  @SuppressWarnings("deprecation") // Internal class warning.
  static <Input, Output> @NonNull Module<Input, Output> immediatelyWrapping(
      @NonNull ThrowingFunction<Input, Output, ?> function
  ) {
    return in -> {
      try {
        return CompletableFuture.completedFuture(new SuccessfulResult<>(function.apply(in)));
      } catch (Throwable throwable) {
        SewerInternalUtilSneakyThrow.sneakyThrow(throwable);
        throw new RuntimeException();
      }
    };
  }

  /**
   * Create a new {@link Module} that filters its input through a {@link Predicate}.
   * <p>
   * If the {@link Predicate} test fails, a {@link FilteredResult} will be returned. This always returns a {@link
   * CompletableFuture#completedFuture completed future}. If the {@link Predicate} test succeeds, this acts as {@link
   * Function#identity()}.
   *
   * @param predicate The predicate to use for filtering this input.
   * @param <Input>   The input type to accept.
   * @return A new {@link Module} filtering the input through a {@link Predicate}.
   */
  static <Input> @NonNull Module<Input, Input> filtering(
      @NonNull Predicate<Input> predicate
  ) {
    return in -> {
      if (!predicate.test(in)) {
        return CompletableFuture.completedFuture(new FilteredResult<>());
      }

      return CompletableFuture.completedFuture(new SuccessfulResult<>(in));
    };
  }

  /**
   * Flow the {@link Input input} through the module, resulting in a {@link Output}.
   *
   * @param input The input to this module.
   * @return A future-wrapped result of an {@link Output}.
   */
  @NonNull CompletableFuture<PipeResult<Output>> flow(Input input);
}
