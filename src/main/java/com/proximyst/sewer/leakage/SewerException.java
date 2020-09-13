package com.proximyst.sewer.leakage;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A swallowed exception during the pumping of a system.
 */
public class SewerException extends RuntimeException {
  @NonNull
  private final String pipeName;

  public SewerException(@NonNull String pipeName, @NonNull Throwable cause) {
    super(cause);
    this.pipeName = pipeName;
  }

  /**
   * The pipe that threw the exception.
   *
   * @return The pipe that threw the exception.
   */
  @NonNull
  public String getPipeName() {
    return pipeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SewerException that = (SewerException) o;
    return getPipeName().equals(that.getPipeName())
        && getCause().equals(that.getCause());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPipeName(), getCause());
  }

  @Override
  public String toString() {
    return "SewerException{" +
        "pipeName='" + pipeName + '\'' +
        "} " + super.toString();
  }
}
