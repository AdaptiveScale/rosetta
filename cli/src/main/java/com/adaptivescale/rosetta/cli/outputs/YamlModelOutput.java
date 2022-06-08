package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Constants;
import com.adaptivescale.rosetta.cli.Output;
import com.adaptivescale.rosetta.common.models.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class YamlModelOutput implements Output<Database> {

    private final Path filePath;

    public YamlModelOutput(String fileName, Path directory) throws IOException {
        Path modelDirectory = directory.resolve(Constants.MODEL_DIRECTORY_NAME);
        Files.createDirectories(modelDirectory);
        this.filePath = modelDirectory.resolve(fileName);
    }

    @Override
    public void write(Database database) throws Exception {
        new ObjectMapper(new YAMLFactory()).writeValue(filePath.toFile(), database);
    }
}
