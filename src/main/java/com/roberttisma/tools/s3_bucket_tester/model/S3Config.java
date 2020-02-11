package com.roberttisma.tools.s3_bucket_tester.model;

import static com.roberttisma.tools.s3_bucket_tester.util.Fields.mergeStringField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3Config implements Mergable<S3Config> {

  private String accessKey;
  private String secretKey;
  private String endpointUrl;
  private String bucket;

  @Override
  public void merge(@NonNull S3Config targetS3Config) {
    mergeStringField(S3Config::getAccessKey, S3Config::setAccessKey, this, targetS3Config);
    mergeStringField(S3Config::getSecretKey, S3Config::setSecretKey, this, targetS3Config);
    mergeStringField(S3Config::getEndpointUrl, S3Config::setEndpointUrl, this, targetS3Config);
    mergeStringField(S3Config::getBucket, S3Config::setBucket, this, targetS3Config);
  }
}
