package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.util.FileIO.checkFileExists;
import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.saveProfile;
import static java.util.Objects.isNull;

import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import com.roberttisma.tools.s3_bucket_tester.util.JsonUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Getter
@RequiredArgsConstructor
@Command(
    name = "set",
    subcommands = {ConfigSetS3Command.class},
    mixinStandardHelpOptions = true,
    description = "Sets a profiles configuration")
public class ConfigSetCommand implements Callable<Integer> {

  @Option(
      names = {"-p", "--profile"},
      description = "Profile to set",
      required = true)
  private String profileName;

  @Option(
      names = {"-f", "--file"},
      description = "Input profile file. Note: --profile value overrides the name field",
      required = false)
  private Path inputFile;

  @Override
  public Integer call() throws Exception {
    ProfileConfig profileConfig = null;
    if (isNull(inputFile)) {
      profileConfig = ProfileConfig.builder().name(profileName).build();
    } else {
      profileConfig = readProfileConfigFile();
      profileConfig.setName(profileName);
    }
    val status = saveProfile(profileConfig);
    System.out.println(status);
    return 0;
  }

  private ProfileConfig readProfileConfigFile() throws IOException {
    checkFileExists(inputFile);
    return JsonUtils.mapper().readValue(inputFile.toFile(), ProfileConfig.class);
  }
}
