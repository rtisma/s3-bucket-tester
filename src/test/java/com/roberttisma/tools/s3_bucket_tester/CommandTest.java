package com.roberttisma.tools.s3_bucket_tester;

import com.roberttisma.tools.s3_bucket_tester.cli.S3BucketTesterCommand;
import org.junit.Test;
import picocli.CommandLine;

public class CommandTest {

  @Test
  public void testCommand() {
    run("config set -p test -f docker/profile-config.json");
    run("run -p test2 -d  docker/s3-bucket-tester-data");
  }

  private void run(String command) {
    new CommandLine(new S3BucketTesterCommand()).execute(command.split("\\s+"));
  }
}
