package com.roberttisma.tools.s3_bucket_tester.cli;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@RequiredArgsConstructor
@Command(
    name = "s3-bucket-tester",
    mixinStandardHelpOptions = true,
    subcommands = {ConfigCommand.class, RunCommand.class},
    description = "Main command")
public class S3BucketTesterCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    CommandLine.usage(this, System.out);
    return 0;
  }
}
