package com.roberttisma.tools.s3_bucket_tester.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {

  @Builder.Default private List<ProfileConfig> profiles = newArrayList();
}
