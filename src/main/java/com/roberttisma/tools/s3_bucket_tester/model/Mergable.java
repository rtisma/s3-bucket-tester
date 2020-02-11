package com.roberttisma.tools.s3_bucket_tester.model;

public interface Mergable<T> {
  void merge(T t);
}
