package com.proximyst.sewer.loadable;

import com.proximyst.sewer.SewerSystem;
import com.proximyst.sewer.filtration.NonNullFiltrationModule;
import com.proximyst.sewer.piping.PipeResult;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A type loaded lazily using a {@link SewerSystem} as a pipeline.
 * <p>
 * This runs everything as async and lazy by default. To run this on the application's "main thread", it must somehow
 * accept tasks through an {@link Executor}.
 *
 * @param <T> The type to be loaded.
 */
public class Loadable<@NonNull T> {
  /**
   * The lock to the inner state.
   */
  @NonNull
  private final Object lock = new Object();

  /**
   * The pipeline to load a {@link T}.
   */
  @NonNull
  private final SewerSystem<@NonNull ?, @NonNull T> pipeline;

  /**
   * The input value to load a {@link T}.
   * <p>
   * This is stored without a generic to avoid code smell. It must still be statically typed without errors in the
   * builder.
   */
  @Nullable
  private final Object object;

  /**
   * The executor on which to run the loading.
   */
  @Nullable
  private final Executor executor;

  /**
   * The local state of the loadable value.
   * <p>
   * This is locked by the {@link #lock}.
   */
  @NonNull
  private volatile LoadableState<PipeResult<T>> state = LoadableState.Unloaded.getInstance();

  /**
   * The {@link CompletableFuture} for the state.
   * <p>
   * If this is {@code null}, it will only be modified by one caller as the rest are locked by the {@link #lock} using a
   * {@code synchronized} block.
   */
  @MonotonicNonNull
  private CompletableFuture<PipeResult<T>> resultFuture = null;

  private Loadable(
      @NonNull SewerSystem<@NonNull ?, @NonNull T> pipeline,
      @Nullable Object object,
      @Nullable Executor executor
  ) {
    this.pipeline = pipeline;
    this.object = object;
    this.executor = executor;
  }

  /**
   * Create a new builder.
   *
   * @param system   The {@link SewerSystem} pipeline to use for loading the value.
   * @param input    The {@link Input} value for loading the value.
   * @param <Input>  The input to accept to load the value. This is not set to {@code null} and is therefore not
   *                 recommended to be a large value one wants garbage collected.
   * @param <Output> The type of the {@link Loadable}.
   * @return A new {@link Builder} to create a new {@link Loadable}.
   */
  @NonNull
  public static <Input, Output> Builder<Input, Output> builder(
      @NonNull SewerSystem<Input, Output> system,
      Input input
  ) {
    return new Builder<>(system, input);
  }

  /**
   * Create a new builder.
   *
   * @param system   The {@link SewerSystem} pipeline to use for loading the value.
   * @param input    The {@link Input} value for loading the value in the form of another {@link Loadable}. The value
   *                 must therefore be loaded through that before it can be loaded with this. If the other {@link
   *                 Loadable} is not already loaded, this will wait for it to load.
   * @param <Input>  The input to accept to load the value. This is not set to {@code null} and is therefore not
   *                 recommended to be a large value one wants garbage collected.
   * @param <Output> The type of the {@link Loadable}.
   * @return A new {@link Builder} to create a new {@link Loadable}.
   */
  @NonNull
  public static <Input, Output> Builder<Loadable<Input>, Output> builder(
      @NonNull SewerSystem<Input, Output> system,
      Loadable<Input> input
  ) {
    return new Builder<>(
        SewerSystem
            .<Loadable<Input>, Input>builder(
                "loadable load",
                in -> in.getOrLoad().thenApply(opt -> opt.orElse(null)),
                null,
                NonNullFiltrationModule.getInstance())
            .pipe("loadable pipeline", in -> system.pump(in).thenApply(res -> res.asOptional().orElse(null)))
            .build(),
        input
    );
  }

  /**
   * Checks whether the internal state has already been filled with a loaded value.
   * <p>
   * If this has not yet been loaded, it will not initiate loading. <i>Only</i> a check is performed.
   *
   * @return Whether the internal state is filled with a loaded value.
   * @see #getIfPresent()
   * @see #getResultIfPresent()
   */
  @SideEffectFree
  public boolean isLoaded() {
    return state instanceof LoadableState.Loaded;
  }

  /**
   * Returns the internal state if it has been filled with a loaded value.
   * <p>
   * If this has not yet been loaded, it will not initiate loading. <i>Only</i> a check is performed before returning
   * its value.
   *
   * @return Whether the internal state is filled with a loaded value.
   * @see #isLoaded()
   * @see #getIfPresent()
   */
  @SideEffectFree
  public Optional<@NonNull PipeResult<T>> getResultIfPresent() {
    if (isLoaded()) {
      return Optional.of(((LoadableState.Loaded<PipeResult<T>>) state).getItem());
    }

    return Optional.empty();
  }

  /**
   * Returns the internal state if it has been filled with a loaded value.
   * <p>
   * If this has not yet been loaded, it will not initiate loading. <i>Only</i> a check is performed before returning
   * its value.
   *
   * @return Whether the internal state is filled with a loaded value.
   * @see #isLoaded()
   * @see #getResultIfPresent()
   */
  @SideEffectFree
  public Optional<T> getIfPresent() {
    return getResultIfPresent()
        .filter(PipeResult::isSuccessful)
        .map(res -> res.asSuccess().getResult());
  }

  /**
   * Gets the internal state or loads it if non-existent before returning its value in the form of a {@link
   * CompletableFuture}.
   * <p>
   * If this has no loaded value, it will initiate one and fill a {@link CompletableFuture} with it. If it is already
   * loaded, an existing, cached {@link CompletableFuture} will be returned instead.
   *
   * @return A future with the {@link PipeResult} of the loading process.
   * @see #isLoaded()
   * @see #getOrLoad()
   */
  // There is no clean way to avoid an input type for the loadable without unchecked casts.
  @SuppressWarnings("unchecked")
  public CompletableFuture<@NonNull PipeResult<T>> getOrLoadResult() {
    if (this.resultFuture != null) {
      return this.resultFuture;
    }

    synchronized (lock) {
      if (this.resultFuture != null) {
        return this.resultFuture;
      }

      this.resultFuture = ((SewerSystem<Object, T>) this.pipeline).pump(this.object, this.executor)
          .thenApply(res -> {
            synchronized (this.lock) {
              this.state = new LoadableState.Loaded<>(res);
            }

            return res;
          });
      return this.resultFuture;
    }
  }

  /**
   * Gets the internal state or loads it if non-existent before returning its value in the form of a {@link
   * CompletableFuture}.
   * <p>
   * If this has no loaded value, it will initiate one and fill a {@link CompletableFuture} with it. If it is already
   * loaded, an existing, cached {@link CompletableFuture} will be returned instead.
   * <p>
   * If the loading process threw an {@link Exception}, the future will be completed with it. Any {@link
   * CompletableFuture#join()} will therefore also throw this {@link Exception}. Any {@code null} values will be thrown
   * out, and if it has been filtered out, {@link Optional#empty()} is returned.
   *
   * @return A future with the returned value of the loading process.
   * @see #isLoaded()
   * @see #getOrLoadResult()
   */
  public CompletableFuture<@NonNull Optional<T>> getOrLoad() {
    return getOrLoadResult().thenCompose(res -> {
      if (res.isExceptional()) {
        CompletableFuture<Optional<T>> future = new CompletableFuture<>();
        future.completeExceptionally(res.asExceptional().getException());
        return future;
      }

      return CompletableFuture.completedFuture(res.asOptional());
    });
  }

  /**
   * A builder to create a new {@link Loadable}.
   *
   * @param <Input>  The input type of the entire loadable. This does not change.
   * @param <Output> The output type of the entire loadable. This does not change.
   */
  public static class Builder<Input, Output> {
    @NonNull
    private final SewerSystem<Input, Output> pipeline;

    private final Input input;

    @Nullable
    private Executor executor = null;

    private Builder(
        @NonNull SewerSystem<Input, Output> pipeline,
        Input input
    ) {
      this.pipeline = pipeline;
      this.input = input;
    }

    /**
     * Set the {@link Executor} of the {@link Loadable}.
     * <p>
     * A value of {@code null} will make it execute on the default {@link Executor}.
     *
     * @param executor The {@link Executor} to use, or {@code null} to use the default for the system.
     * @return This builder with the new executor set.
     */
    public Builder<Input, Output> executor(@Nullable Executor executor) {
      this.executor = executor;
      return this;
    }

    /**
     * Build the {@link Loadable}.
     *
     * @return A new {@link Loadable} akin this builder.
     */
    public Loadable<Output> build() {
      return new Loadable<>(
          this.pipeline,
          this.input,
          this.executor
      );
    }
  }
}
