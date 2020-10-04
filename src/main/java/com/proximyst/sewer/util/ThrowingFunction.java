package com.proximyst.sewer.util;

@FunctionalInterface
public interface ThrowingFunction<In, Out, Thr extends Throwable> {
  Out accept(In input) throws Thr;
}
