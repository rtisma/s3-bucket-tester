package com.roberttisma.tools.s3_bucket_tester.service;

import static java.util.concurrent.Executors.newFixedThreadPool;

import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Builder
@RequiredArgsConstructor
public class ProcessService implements Runnable {

  @NonNull private final ProfileConfig profileConfig;
  private final int numThreads;

  @Override
  @SneakyThrows
  public void run() {
    val executorService = newFixedThreadPool(numThreads);
    executorService.shutdown();
    executorService.awaitTermination(1L, TimeUnit.DAYS);
  }
}
