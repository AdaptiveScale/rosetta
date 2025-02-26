package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class DbtSqlModelOutput implements Output<Map<String, String>> {
  private final Path filePath;

  public DbtSqlModelOutput(Path directory) {
    this.filePath = directory;
  }

  public Path getFilePath() {
    return filePath;
  }

  @Override
  public void write(Map<String, String> dbtSQLTables) {
    dbtSQLTables.forEach((tableKey, tableString) -> {
      FileOutputStream fileOutputStream;
      try {
        Path resolvedFilePath = filePath.resolve("models").resolve(tableKey + ".sql");
        fileOutputStream = new FileOutputStream(resolvedFilePath.toFile());
        fileOutputStream.write(tableString.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}