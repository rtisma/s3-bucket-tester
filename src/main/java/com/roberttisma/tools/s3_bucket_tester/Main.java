package com.roberttisma.tools.s3_bucket_tester;

import com.roberttisma.tools.s3_bucket_tester.cli.S3BucketTesterCommand;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
public class Main {

  public static void main(String[] args) throws IOException {
    new CommandLine(new S3BucketTesterCommand()).execute(args);
  }
}
