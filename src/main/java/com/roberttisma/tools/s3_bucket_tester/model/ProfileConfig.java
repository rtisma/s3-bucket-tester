package com.roberttisma.tools.s3_bucket_tester.model;

import static com.roberttisma.tools.s3_bucket_tester.util.Fields.mergeMergableField;
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
public class ProfileConfig implements Mergable<ProfileConfig> {
  private String name;

  @Builder.Default private S3Config s3 = new S3Config();

  @Override
  public void merge(@NonNull ProfileConfig mergeIn) {
    mergeStringField(ProfileConfig::getName, ProfileConfig::setName, this, mergeIn);
    mergeMergableField(ProfileConfig::getS3, this, mergeIn);
  }
}
