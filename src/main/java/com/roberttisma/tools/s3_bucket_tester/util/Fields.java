package com.roberttisma.tools.s3_bucket_tester.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

import com.roberttisma.tools.s3_bucket_tester.model.Mergable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.val;

public class Fields {

  public static <T> void mergeIntegerField(
      Function<T, Integer> getter, BiConsumer<T, Integer> setter, T base, T input) {
    val value = getter.apply(input);

    if (!isNull(value)) {
      setter.accept(base, value);
    }
  }

  public static <T> void mergeStringField(
      Function<T, String> getter, BiConsumer<T, String> setter, T base, T input) {
    val value = getter.apply(input);

    if (!isNullOrEmpty(value)) {
      setter.accept(base, value);
    }
  }

  public static <T, M extends Mergable<M>> void mergeMergableField(
      Function<T, M> getter, T base, T input) {
    val value = getter.apply(input);

    if (!isNull(value)) {
      getter.apply(base).merge(value);
    }
  }
}
