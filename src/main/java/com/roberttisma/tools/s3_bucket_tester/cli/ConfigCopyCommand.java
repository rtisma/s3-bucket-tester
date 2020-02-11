package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.copyProfile;

import com.roberttisma.tools.s3_bucket_tester.exceptions.S3TesterException;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@RequiredArgsConstructor
@Command(name = "cp", mixinStandardHelpOptions = true, description = "Copies a profile")
public class ConfigCopyCommand implements Callable<Integer> {

  @Option(
      names = {"-p", "--profile"},
      description = "Profile to copy",
      required = true)
  private String profileName;

  @Option(
      names = {"-n", "--name"},
      description = "Name of copy",
      required = true)
  private String copyName;

  @Override
  public Integer call() throws Exception {
    try {
      copyProfile(profileName, copyName);
      System.out.println("Copied profile " + profileName + " to " + copyName);
    } catch (S3TesterException e) {
      log.error(e.getMessage());
      System.err.println(e.getMessage());
      return 1;
    }
    return 0;
  }
}
