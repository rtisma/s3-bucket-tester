package com.roberttisma.tools.s3_bucket_tester.util;

import static com.roberttisma.tools.s3_bucket_tester.exceptions.S3TesterException.checkImporter;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberttisma.tools.s3_bucket_tester.model.Config;
import com.roberttisma.tools.s3_bucket_tester.model.ProfileConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ProfileManager {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static Config readConfig() throws IOException {
    val filePath = getConfigFilePath();
    if (exists(filePath)) {
      return OBJECT_MAPPER.readValue(filePath.toFile(), Config.class);
    }
    return Config.builder().build();
  }

  public static Optional<ProfileConfig> findProfile(@NonNull String profileName)
      throws IOException {
    return findProfile(readConfig(), profileName);
  }

  public static Optional<ProfileConfig> findProfile(
      @NonNull Config config, @NonNull String profileName) {
    return config.getProfiles().stream().filter(x -> x.getName().equals(profileName)).findFirst();
  }

  public static void copyProfile(@NonNull String profileName, @NonNull String copyName)
      throws IOException {
    var config = readConfig();
    checkProfileExist(config, profileName);
    val profile = findProfile(profileName).get();
    profile.setName(copyName);
    saveProfile(profile);
  }

  public static void deleteProfile(@NonNull String profileName) throws IOException {
    var config = readConfig();
    checkProfileExist(config, profileName);
    config.getProfiles().removeIf(x -> x.getName().equals(profileName));
    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(getConfigFilePath().toFile(), config);
  }

  private static void checkProfileExist(Config c, String profileName) {
    val result =
        c.getProfiles().stream().map(ProfileConfig::getName).anyMatch(x -> x.equals(profileName));
    checkImporter(result, "The profile '%s' does not exist", profileName);
  }

  public static String saveProfile(@NonNull ProfileConfig profileConfigToSave) throws IOException {
    var config = readConfig();
    val existingProfileResult = findProfile(config, profileConfigToSave.getName());
    if (existingProfileResult.isPresent()) {
      val base = existingProfileResult.get();
      base.merge(profileConfigToSave);
    } else {
      config.getProfiles().add(profileConfigToSave);
      //      config = Config.builder().profile(profileConfigToSave).build();
    }
    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(getConfigFilePath().toFile(), config);
    return format(
        "%s profile: %s",
        existingProfileResult.isPresent() ? "Updated" : "Created", profileConfigToSave.getName());
  }

  public static Path getConfigFilePath() throws IOException {
    return getConfigDirPath().resolve("config.json");
  }

  private static Path getConfigDirPath() throws IOException {
    val homeDir = Paths.get(System.getProperty("user.home"));
    checkImporter(
        isDirectory(homeDir), "The user.home directory '%s' does not exist", homeDir.toString());
    val outDir = homeDir.resolve(".s3-bucket-tester");
    if (!Files.isDirectory(outDir)) {
      createDirectories(outDir);
    }
    return outDir;
  }
}
