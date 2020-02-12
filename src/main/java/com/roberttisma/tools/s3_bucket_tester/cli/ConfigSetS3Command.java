package com.roberttisma.tools.s3_bucket_tester.cli;

import static com.roberttisma.tools.s3_bucket_tester.util.ProfileManager.saveProfile;

import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import com.roberttisma.tools.s3_bucket_tester.model.S3Config;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Slf4j
@RequiredArgsConstructor
@Command(name = "s3", mixinStandardHelpOptions = true, description = "Sets the s3 parameters")
public class ConfigSetS3Command implements Callable<Integer> {

  @ParentCommand private ConfigSetCommand configSetCommand;

  @Option(
      names = {"-a", "--access-key"},
      interactive = false,
      description = "Set the access key for s3",
      required = false)
  private String accessKey;

  @Option(
      names = {"-s", "--secret-key"},
      interactive = false,
      description = "Set the secret key for s3",
      required = false)
  private String secretKey;

  @Option(
      names = {"-u", "--endpoint-url"},
      description = "Set s3 endpoint url",
      required = false)
  private String endpointUrl;

  @Option(
      names = {"-b", "--bucket"},
      description = "Set s3 bucket to test",
      required = false)
  private String bucket;

  @Override
  public Integer call() throws Exception {
    val profileConfig =
        ProfileConfig.builder()
            .name(configSetCommand.getProfileName())
            .s3(
                S3Config.builder()
                    .accessKey(accessKey)
                    .secretKey(secretKey)
                    .endpointUrl(endpointUrl)
                    .bucket(bucket)
                    .build())
            .build();
    val status = saveProfile(profileConfig);
    System.out.println(status);
    return 0;
  }
}
