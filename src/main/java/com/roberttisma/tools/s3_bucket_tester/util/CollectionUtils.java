package com.roberttisma.tools.s3_bucket_tester.util;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.NonNull;

public class CollectionUtils {

  public static <T, R> List<R> mapToList(
      @NonNull Collection<T> values, @NonNull Function<T, R> function) {
    return mapToStream(values, function).collect(toList());
  }

  public static <T, R> Stream<R> mapToStream(
      @NonNull Collection<T> values, @NonNull Function<T, R> function) {
    return values.stream().map(function);
  }

  public static <T, R> Set<R> mapToSet(
      @NonNull Collection<T> values, @NonNull Function<T, R> function) {
    return mapToStream(values, function).collect(toSet());
  }
}
