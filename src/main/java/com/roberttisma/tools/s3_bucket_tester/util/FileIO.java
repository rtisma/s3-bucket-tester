package com.roberttisma.tools.s3_bucket_tester.util;

import static com.google.common.io.Files.toByteArray;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.val;

public class FileIO {

  public static Optional<String> statusPathDoesNotExist(@NonNull Path path) {
    if (!exists(path)) {
      return Optional.of(format("The path '%s' does not exist", path));
    }
    return Optional.empty();
  }

  public static Optional<String> statusFileDoesNotExist(@NonNull Path filepath) {
    return statusPathDoesNotExist(filepath)
        .filter(x -> !isRegularFile(filepath))
        .map(x -> format("The path '%s' is not a file", x));
  }

  public static Optional<String> statusDirectoryDoesNotExist(@NonNull Path dirpath) {
    return statusPathDoesNotExist(dirpath)
        .filter(x -> !isDirectory(dirpath))
        .map(x -> format("The path '%s' is not a directory", x));
  }

  public static void setupDirectory(@NonNull Path dirpath) throws IOException {
    if (!isDirectory(dirpath)) {
      Files.createDirectories(dirpath);
    }
  }

  public static void checkPathExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusPathDoesNotExist(path));
  }

  public static void checkFileExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusFileDoesNotExist(path));
  }

  public static void checkDirectoryExists(@NonNull Path path) throws IOException {
    pathChecker(() -> statusDirectoryDoesNotExist(path));
  }

  public static String readFileContent(@NonNull Path filePath) throws IOException {
    return new String(toByteArray(filePath.toFile()));
  }

  public static Stream<Path> streamFilesInDir(@NonNull Path dirPath, boolean recursive)
      throws IOException {
    checkDirectoryExists(dirPath);
    return (recursive ? walk(dirPath) : walk(dirPath, 1)).filter(x -> !isDirectory(x));
  }

  public static List<Path> listFilesInDir(@NonNull Path dirPath, boolean recursive)
      throws IOException {
    return streamFilesInDir(dirPath, recursive).collect(toList());
  }

  private static void pathChecker(Supplier<Optional<String>> statusSupplier) throws IOException {
    val result = statusSupplier.get();
    if (result.isPresent()) {
      throw new IOException(result.get());
    }
  }
}
