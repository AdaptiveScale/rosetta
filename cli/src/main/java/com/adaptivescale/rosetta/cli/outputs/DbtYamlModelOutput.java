package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Output;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.nio.file.Path;

public class DbtYamlModelOutput implements Output<DbtModel> {
  private final Path filePath;

  public DbtYamlModelOutput(String fileName, Path directory) {
    this.filePath = directory.resolve(fileName);
  }

  public Path getFilePath() {
    return filePath;
  }
  @Override
  public void write(DbtModel model) {
    try {
      new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)).writeValue(filePath.toFile(), model);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}