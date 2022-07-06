package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Output;
import com.adaptivescale.rosetta.common.models.Database;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Path;

public class YamlModelOutput implements Output<Database> {
    private final Path filePath;

    public Path getFilePath() {
        return filePath;
    }

    public YamlModelOutput(String fileName, Path directory) {
        this.filePath = directory.resolve(fileName);
    }

    @Override
    public void write(Database database) throws Exception {
        new ObjectMapper(new YAMLFactory()).setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValue(filePath.toFile(), database);
    }
}
