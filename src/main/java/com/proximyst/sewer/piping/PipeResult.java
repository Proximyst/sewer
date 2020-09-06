package com.proximyst.sewer.piping;

import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The result of a pipe's flow.
 *
 * @param <T> The output type of this pipe.
 */
public class PipeResult<T> {
  @NonNull
  private final String pipeName;

  private PipeResult(@NonNull String pipeName) {
    this.pipeName = pipeName;
  }

  /**
   * Constructs a new successful result of the pipe name given with its result.
   *
   * @param pipeName The pipe whose flow was successful.
   * @param result   The result of the pipe's flow.
   * @param <T>      The type of the pipe's flow.
   * @return A successful result for the pipe.
   */
  public static <T> Success<T> success(@NonNull String pipeName, T result) {
    return new Success<>(pipeName, result);
  }

  /**
   * Constructs a new failed result of the pipe name given with its thrown {@link Exception}.
   *
   * @param pipeName  The pipe whose flow was erroneous.
   * @param exception The exception the pipe threw.
   * @param <T>       The type of the pipe's flow.
   * @return A failed result for the pipe.
   */
  public static <T> Exceptional<T> exceptional(@NonNull String pipeName, @NonNull Exception exception) {
    return new Exceptional<>(pipeName, exception);
  }

  /**
   * Constructs a new result which stopped before running due to its filters.
   *
   * @param pipeName The pipe whose flow was stopped.
   * @param <T>      The type of the pipe's flow.
   * @return A failed result for the pipe.
   */
  public static <T> PreFilter<T> beforeFilter(@NonNull String pipeName) {
    return new PreFilter<>(pipeName);
  }

  /**
   * Constructs a new result which stopped after running due to its filters.
   * <p>
   * The flow of the pipe is still ran, but it will not continue to the next pipe or return if successful.
   *
   * @param pipeName The pipe whose return was stopped.
   * @param <T>      The type of the pipe's flow.
   * @return A failed result for the pipe.
   */
  public static <T> PostFilter<T> postFilter(@NonNull String pipeName, T output) {
    return new PostFilter<>(pipeName, output);
  }

  /**
   * @return Whether the pipe flow was successful.
   */
  public boolean isSuccessful() {
    return this instanceof Success;
  }

  /**
   * @return Whether the pipe flow threw an exception.
   */
  public boolean isExceptional() {
    return this instanceof Exceptional;
  }

  /**
   * @return Whether the pipe flow was filtered.
   */
  public boolean isFiltered() {
    return this instanceof PreFilter
        || this instanceof PostFilter;
  }

  /**
   * @return Whether the pipe flow was at all unsuccessful.
   */
  public boolean isFailed() {
    return !isSuccessful();
  }

  /**
   * @return The name of the pipe whose flow is in question.
   */
  @NonNull
  public String getPipeName() {
    return this.pipeName;
  }

  /**
   * @return Casts this to a {@link Success}.
   * @throws ClassCastException If {@link #isSuccessful()} returns {@code false}.
   * @see #isSuccessful()
   */
  public Success<T> asSuccess() {
    // We want a ClassCastException here.
    // If the user has not first ran #isSuccessful, that is on them...
    return (Success<T>) this;
  }

  /**
   * @return Casts this to an {@link Exceptional}.
   * @throws ClassCastException If {@link #isExceptional()} returns {@code false}.
   * @see #isExceptional()
   */
  public Exceptional<T> asExceptional() {
    // We want a ClassCastException here.
    // If the user has not first ran #isExceptional, that is on them...
    return (Exceptional<T>) this;
  }

  /**
   * Converts this to an {@link Optional}.
   * <p>
   * This will <i>swallow</i> any {@code null}s as {@link Optional#empty()}!
   *
   * @return An {@link Optional} of the result from this. This is only non-empty if {@link #isSuccessful() this is
   * successful} and {@link Success#getResult() the result} is non-{@code null}.
   */
  @NonNull
  public Optional<@NonNull T> asOptional() {
    if (!isSuccessful()) {
      return Optional.empty();
    }

    return Optional.ofNullable(asSuccess().getResult());
  }

  /**
   * A successful pipe flow.
   *
   * @param <T> The type of the pipe's flow.
   */
  public static final class Success<T> extends PipeResult<T> {
    private final T result;

    private Success(@NonNull String pipeName, T result) {
      super(pipeName);
      this.result = result;
    }

    /**
     * @return The pipe flow's result.
     */
    public T getResult() {
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Success<?> success = (Success<?>) o;
      return Objects.equals(getResult(), success.getResult())
          && getPipeName().equals(success.getPipeName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getPipeName(), getResult());
    }

    @Override
    public String toString() {
      return "Success{" +
          "pipeName='" + getPipeName() + '\'' +
          ", result=" + getResult() +
          '}';
    }
  }

  /**
   * An erroneous pipe flow throwing an exception.
   *
   * @param <T> The type of the pipe's flow.
   */
  public static final class Exceptional<T> extends PipeResult<T> {
    @NonNull
    private final Exception exception;

    private Exceptional(
        @NonNull String pipeName,
        @NonNull Exception exception
    ) {
      super(pipeName);
      this.exception = exception;
    }

    /**
     * @return The exception thrown by the pipe flow.
     */
    @NonNull
    public Exception getException() {
      return exception;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Exceptional<?> that = (Exceptional<?>) o;
      return getException().equals(that.getException())
          && getPipeName().equals(that.getPipeName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getPipeName(), getException());
    }

    @Override
    public String toString() {
      return "Exceptional{" +
          "pipeName='" + getPipeName() + '\'' +
          ", exception=" + getException() +
          '}';
    }
  }

  /**
   * An erroneous pipe flow filtered before execution.
   *
   * @param <T> The type of the pipe's flow.
   */
  public static final class PreFilter<T> extends PipeResult<T> {
    private PreFilter(@NonNull String pipeName) {
      super(pipeName);
    }

    @Override
    public boolean equals(Object o) {
      return this == o || o instanceof PreFilter && this.getPipeName().equals(((PreFilter<?>) o).getPipeName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getPipeName());
    }

    @Override
    public String toString() {
      return "PreFilter{" +
          "pipeName='" + getPipeName() + '\'' +
          '}';
    }
  }

  /**
   * An erroneous pipe flow filtered after execution.
   *
   * @param <T> The type of the pipe's flow.
   */
  public static final class PostFilter<T> extends PipeResult<T> {
    private final T output;

    private PostFilter(
        @NonNull String pipeName,
        T output
    ) {
      super(pipeName);
      this.output = output;
    }

    /**
     * @return The output of the flow before it was filtered.
     */
    public T getOutput() {
      return output;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PostFilter<?> that = (PostFilter<?>) o;
      return Objects.equals(getOutput(), that.getOutput())
          && getPipeName().equals(that.getPipeName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getPipeName(), getOutput());
    }

    @Override
    public String toString() {
      return "PostFilter{" +
          "pipeName='" + getPipeName() + '\'' +
          ", output=" + getOutput() +
          '}';
    }
  }
}
