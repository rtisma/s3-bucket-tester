package com.roberttisma.tools.s3_bucket_tester.service;

import static java.util.Comparator.comparingLong;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toUnmodifiableList;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Builder
@RequiredArgsConstructor
public class ProcessService implements Runnable {
  private static final String DATA = "w";

  @NonNull private final AmazonS3 client;
  @NonNull private final String bucketName;
  private final int numThreads;
  private final int numObjects;
  private final int monitorPeriod;

  @Override
  @SneakyThrows
  public void run() {
    val executorService = newFixedThreadPool(numThreads);
//    val scheduledExecutorService = Executors.newScheduledThreadPool(1);
    val ids = IntStream.range(0, numObjects).boxed().collect(toUnmodifiableList());
    val runners =
        Lists.partition(ids, ids.size()/numThreads).stream()
            .map(x -> new Runner(x, client, bucketName, monitorPeriod))
            .collect(toUnmodifiableList());
    runners.forEach(executorService::submit);
//    scheduledExecutorService.scheduleAtFixedRate(
//        () -> this.report(runners),2, samplePeriodSeconds, SECONDS);
    executorService.shutdown();
    executorService.awaitTermination(15L, TimeUnit.MINUTES);
//    scheduledExecutorService.shutdown();
//    scheduledExecutorService.awaitTermination(15L, TimeUnit.MINUTES);
  }

//  private void report(List<Runner> runners) {
//    val reports = runners.stream().map(Runner::getReport).collect(toUnmodifiableList());
//    val maxDuration = reports.stream().max(comparingLong(RunReport::getDuration));
//    val totalCount = reports.stream().mapToInt(RunReport::getCount).sum();
//    val averageThreadSpeed = calculateAverageThreadSpeed(reports);
//    log.info(
//        "[MONITOR]: totalCount: {}   maxDuration: {}   averageThreadSpeed: {}",
//        totalCount,
//        maxDuration,
//        averageThreadSpeed);
//  }

  private static double calculateAverageThreadSpeed(List<RunReport> reports) {
    return reports.stream()
        .mapToDouble(x -> (double) x.getCount() / (reports.size() * x.getDuration()))
        .sum();
  }

  @RequiredArgsConstructor
  private static class Runner implements Runnable {
    @NonNull private final List<Integer> ids;
    @NonNull private final AmazonS3 client;
    @NonNull private final String bucketName;
    private final int monitorPeriod;

    private int count = 0;
    private boolean started = false;
    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    private void startWatch(){
      if (!stopwatch.isRunning()){
        stopwatch.start();
      }
    }

    private void stopWatch(){
      if (stopwatch.isRunning()){
        stopwatch.stop();
      }
    }

    @Override
    public void run() {
      startWatch();
      for (val id : ids){
        runId(id);
        if (count % monitorPeriod == 0){
          stopWatch();
          val duration = stopwatch.elapsed(SECONDS);
          val speed = (double)count/duration;
          val threadName = Thread.currentThread().getName();
          log.info(
              "[MONITOR-{}]: count: {} req   duration: {}s   speed: {} req/s",
              threadName,
              count,
              duration,
              speed);
          startWatch();
        }
      }
      stopWatch();
    }


    private void runId(int id) {
      try {
        val key = "data/some-very-long-id-" + id;
        client.putObject(bucketName, key, DATA);
        count++;
      } catch (Throwable t) {
        log.error(t.getMessage());
      }
    }
  }

  @Value
  @Builder
  private static class RunReport {
    private final long duration;
    private final int count;
  }

}
