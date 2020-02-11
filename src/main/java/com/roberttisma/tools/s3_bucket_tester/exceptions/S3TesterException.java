package com.roberttisma.tools.s3_bucket_tester.exceptions;

import static java.lang.String.format;

public class S3TesterException extends RuntimeException {

  public S3TesterException() {}

  public S3TesterException(String message) {
    super(message);
  }

  public S3TesterException(String message, Throwable cause) {
    super(message, cause);
  }

  public S3TesterException(Throwable cause) {
    super(cause);
  }

  public S3TesterException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public static void checkImporter(boolean expression, String formattedString, Object... args) {
    if (!expression) {
      throw buildImporterException(formattedString, args);
    }
  }

  public static S3TesterException buildImporterException(String formattedString, Object... args) {
    return new S3TesterException(format(formattedString, args));
  }
}
