package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.findProfile;
import static java.lang.String.format;

import com.roberttisma.tools.s3_bucket_tester.service.ProcessService;
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

  @Override
  public Integer call() throws Exception {
    val result = findProfile(profileName);
    if (result.isPresent()) {
      val profileConfig = result.get();
      ProcessService.builder().profileConfig(profileConfig).numThreads(numThreads).build().run();
    } else {
      val errorMessage = format("The profile '%s does not exist'", profileName);
      log.error(errorMessage);
      System.out.println(errorMessage);
      return 1;
    }
    return 0;
  }
}
