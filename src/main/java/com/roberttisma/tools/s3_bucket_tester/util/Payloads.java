package com.roberttisma.tools.s3_bucket_tester.util;

import static com.roberttisma.tools.s3_bucket_tester.util.JsonUtils.checkRequiredField;
import static com.roberttisma.tools.s3_bucket_tester.util.JsonUtils.readTree;

import java.nio.file.Path;
import lombok.val;

public class Payloads {

  private static final String STUDY_ID = "studyId";

  public static String parseStudyId(Path jsonPath) {
    val root = readTree(jsonPath);
    checkRequiredField(root, STUDY_ID);
    return root.path(STUDY_ID).asText();
  }
}
