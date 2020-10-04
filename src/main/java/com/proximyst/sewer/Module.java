package com.proximyst.sewer;

import com.proximyst.sewer.piping.FilteredResult;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.SuccessfulResult;
import com.proximyst.sewer.util.SewerInternalUtilSneakyThrow;
import com.proximyst.sewer.util.ThrowingFunction;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A module for a pipe.
 *
 * @param <Input>  The type to accept when flowing through the module.
 * @param <Output> The type the module shall output.
 */
@FunctionalInterface
public interface Module<Input, Output> {
  static <Input, Output> @NonNull Module<Input, Output> immediately(
      @NonNull ThrowingFunction<Input, @NonNull PipeResult<Output>, ?> function
  ) {
    return in -> {
      try {
        return CompletableFuture.completedFuture(function.accept(in));
      } catch (Throwable throwable) {
        SewerInternalUtilSneakyThrow.sneakyThrow(throwable);
        throw new RuntimeException();
      }
    };
  }

  static <Input, Output> @NonNull Module<Input, Output> immediatelyWrapping(
      @NonNull ThrowingFunction<Input, Output, ?> function
  ) {
    return in -> {
      try {
        return CompletableFuture.completedFuture(new SuccessfulResult<>(function.accept(in)));
      } catch (Throwable throwable) {
        SewerInternalUtilSneakyThrow.sneakyThrow(throwable);
        throw new RuntimeException();
      }
    };
  }

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
