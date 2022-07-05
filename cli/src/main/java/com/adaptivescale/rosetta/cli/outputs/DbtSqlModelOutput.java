package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Constants;
import com.adaptivescale.rosetta.cli.Output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DbtSqlModelOutput implements Output<Map<String, String>> {
  private final Path filePath;

  public DbtSqlModelOutput(Path directory) throws IOException {
    Path modelDirectory = directory.resolve(Constants.DBT_DIRECTORY_NAME).resolve(Constants.DBT_MODEL_DIRECTORY_NAME);
    Files.createDirectories(modelDirectory);
    this.filePath = modelDirectory;
  }

  public Path getFilePath() {
    return filePath;
  }

  @Override
  public void write(Map<String, String> dbtSQLTables) {
    dbtSQLTables.forEach((tableKey, tableString) -> {
      FileOutputStream fileOutputStream;
      try {
        Path resolvedFilePath = filePath.resolve(tableKey + ".sql");
        fileOutputStream = new FileOutputStream(resolvedFilePath.toFile());
        fileOutputStream.write(tableString.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
