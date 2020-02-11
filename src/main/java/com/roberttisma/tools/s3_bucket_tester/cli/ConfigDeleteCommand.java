package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.deleteProfile;

import com.roberttisma.tools.s3_bucket_tester.exceptions.S3TesterException;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@RequiredArgsConstructor
@Command(name = "rm", mixinStandardHelpOptions = true, description = "Deletes a profile")
public class ConfigDeleteCommand implements Callable<Integer> {

  @Option(
      names = {"-p", "--profile"},
      description = "Profile to delete",
      required = true)
  private String profileName;

  @Override
  public Integer call() throws Exception {
    try {
      deleteProfile(profileName);
      System.out.println("Deleted profile " + profileName);
    } catch (S3TesterException e) {
      log.error(e.getMessage());
      System.err.println(e.getMessage());
      return 1;
    }
    return 0;
  }
}
