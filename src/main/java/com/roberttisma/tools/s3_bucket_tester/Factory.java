package com.roberttisma.tools.s3_bucket_tester;

import static java.time.temporal.ChronoUnit.SECONDS;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.RetryPolicy;

@Slf4j
public class Factory {

  public static <T> RetryPolicy<T> createRetry(@NonNull Class<T> type) {
    return new RetryPolicy<T>()
        .withMaxRetries(3)
        .onFailedAttempt(
            executionAttemptedEvent -> {
              val message = executionAttemptedEvent.getLastFailure().getMessage();
              val type1 = executionAttemptedEvent.getLastFailure().getClass().getSimpleName();
              log.error("Failed attempt -> [{}]: {}", type1, message);
            })
        .onRetry(
            executionAttemptedEvent ->
                log.warn("Failure # {}. Retrying", executionAttemptedEvent.getAttemptCount()))
        .withBackoff(1, 30, SECONDS);
  }
}
