package com.roberttisma.tools.s3_bucket_tester.util;

import static kong.unirest.HeaderNames.AUTHORIZATION;
import static kong.unirest.HeaderNames.CONTENT_TYPE;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.NonNull;

public class RestClient {

  public static HttpResponse<String> get(@NonNull String accessToken, @NonNull String url) {
    return Unirest.get(url)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .header(CONTENT_TYPE, "application/json")
        .asString();
  }

  public static HttpResponse<String> get(@NonNull String url) {
    return Unirest.get(url).header(CONTENT_TYPE, "application/json").asString();
  }

  public static <T> HttpResponse<String> post(
      @NonNull String accessToken, @NonNull String url, @NonNull T body) {
    return Unirest.post(url)
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .header(CONTENT_TYPE, "application/json")
        .body(body)
        .asString();
  }
}
