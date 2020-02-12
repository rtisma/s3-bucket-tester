package com.roberttisma.tools.s3_bucket_tester;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import com.roberttisma.tools.s3_bucket_tester.model.S3Config;
import com.roberttisma.tools.s3_bucket_tester.service.ProcessService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jodah.failsafe.RetryPolicy;

@Slf4j
public class Factory {

  public static ProcessService buildProcessService(
      ProfileConfig c, int numObjects, int numThreads, int monitorPeriod) {
    return ProcessService.builder()
        .client(buildS3Client(c.getS3()))
        .bucketName(c.getS3().getBucket())
        .numObjects(numObjects)
        .numThreads(numThreads)
        .monitorPeriod(monitorPeriod)
        .build();
  }

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

  private static AmazonS3 buildS3Client(@NonNull S3Config s3Config) {
    val client =
        new AmazonS3Client(
            new BasicAWSCredentials(s3Config.getAccessKey(), s3Config.getSecretKey()),
            clientConfiguration());
    client.setEndpoint(s3Config.getEndpointUrl());
    client.setS3ClientOptions(new S3ClientOptions().withPathStyleAccess(true));
    return client;
  }

  private static ClientConfiguration clientConfiguration() {
    ClientConfiguration clientConfiguration = new ClientConfiguration();

    clientConfiguration.setSignerOverride("AWSS3V4SignerType");
    log.info("Using AWSS3V4SignerType");

    clientConfiguration.setProtocol(Protocol.HTTPS);
    clientConfiguration.setRetryPolicy(
        PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(5));
    clientConfiguration.setConnectionTimeout(15000);
    return clientConfiguration;
  }
}
