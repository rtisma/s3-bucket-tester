package com.roberttisma.tools.s3_bucket_tester.cli;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Slf4j
@RequiredArgsConstructor
@Command(
    name = "config",
    mixinStandardHelpOptions = true,
    subcommands = {
      ConfigSetCommand.class,
      ConfigGetCommand.class,
      ConfigDeleteCommand.class,
      ConfigCopyCommand.class
    },
    description = "Configures the tool")
public class ConfigCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    CommandLine.usage(this, System.out);
    return 0;
  }
}
