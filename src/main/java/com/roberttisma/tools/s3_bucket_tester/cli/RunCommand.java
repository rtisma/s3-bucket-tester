package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.Factory.buildProcessService;
import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.findProfile;
import static java.lang.String.format;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@RequiredArgsConstructor
@Command(
    name = "run",
    mixinStandardHelpOptions = true,
    description = "Runs an import for a specific profile")
public class RunCommand implements Callable<Integer> {

  @Option(
      names = {"-p", "--profile"},
      description = "Profile to set",
      required = true)
  private String profileName;

  @Option(
      names = {"-t", "--threads"},
      description = "Number of threads to use. Default: ${DEFAULT-VALUE}",
      defaultValue = "1",
      required = false)
  private int numThreads;

  @Option(
      names = {"-n", "--num-objects"},
      description = "Number of objects to upload",
      required = true)
  private int numObjects;

  @Option(
      names = {"-m", "--monitor-period"},
      description = "Log the performance every <m> requests. Default: ${DEFAULT-VALUE} ",
      defaultValue = "1000",
      required = false)
  private int monitorPeriod;

  @Override
  public Integer call() throws Exception {
    val result = findProfile(profileName);
    if (result.isPresent()) {
      val profileConfig = result.get();
      buildProcessService(profileConfig, numObjects, numThreads, monitorPeriod).run();
    } else {
      val errorMessage = format("The profile '%s does not exist'", profileName);
      log.error(errorMessage);
      System.out.println(errorMessage);
      return 1;
    }
    return 0;
  }
}
