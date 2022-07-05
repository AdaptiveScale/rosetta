package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Constants;
import com.adaptivescale.rosetta.cli.Output;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DbtYamlModelOutput implements Output<DbtModel> {
  private final Path filePath;

  public DbtYamlModelOutput(String fileName, Path directory) throws IOException {
    Path modelDirectory = directory.resolve(Constants.DBT_DIRECTORY_NAME);
    Files.createDirectories(modelDirectory);
    this.filePath = modelDirectory.resolve(fileName);
  }

  public Path getFilePath() {
    return filePath;
  }
  @Override
  public void write(DbtModel model) throws Exception {
    new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)).writeValue(filePath.toFile(), model);
  }
}
