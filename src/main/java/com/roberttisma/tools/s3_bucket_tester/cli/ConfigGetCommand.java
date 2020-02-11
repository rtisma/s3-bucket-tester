package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.roberttisma.tools.s3_bucket_tester.util.CollectionUtils.mapToList;
import static com.roberttisma.tools.s3_bucket_tester.util.JsonUtils.toPrettyJson;
import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.findProfile;
import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.readConfig;
import static java.lang.String.format;

import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@RequiredArgsConstructor
@Command(
    name = "get",
    mixinStandardHelpOptions = true,
    description = "Gets a profiles configuration")
public class ConfigGetCommand implements Callable<Integer> {

  @ArgGroup(exclusive = true, multiplicity = "1")
  Exclusive exclusive;

  private static class Exclusive {
    @Option(
        names = {"-p", "--profile"},
        description = "Profile to set",
        required = true)
    private String profileName;

    @Option(
        names = {"-l", "--list"},
        description = "List profiles",
        required = true)
    private boolean listProfiles;
  }

  @Override
  public Integer call() throws Exception {
    if (exclusive.listProfiles) {
      System.out.println(
          toPrettyJson(mapToList(readConfig().getProfiles(), ProfileConfig::getName)));
    } else if (!isNullOrEmpty(exclusive.profileName)) {
      val result = findProfile(readConfig(), exclusive.profileName);
      if (result.isPresent()) {
        System.out.println(toPrettyJson(result.get()));
      } else {
        val errorMessage =
            format("[ERROR]: the profile '%s' does not exist", exclusive.profileName);
        System.out.println(errorMessage);
        log.debug(errorMessage);
        return 1;
      }
    } else {
      val errorMessage = "[ERROR]: should not be here";
      System.out.println(errorMessage);
      log.error(errorMessage);
      return 1;
    }
    return 0;
  }
}
