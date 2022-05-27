package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.common.models.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.file.Path;

public class DirectoryOutput implements Output {
    private final Path filePath;

    public DirectoryOutput(String sourceName, String targetName, Path directory) {
        String fileName = String.format("ddl-%s%s.yaml", sourceName,
                (targetName == null || targetName.isEmpty()) ? "" : "-" + targetName);
        this.filePath = directory.resolve(fileName);
    }

    @Override
    public void write(Database database) throws Exception {
        new ObjectMapper(new YAMLFactory()).writeValue(filePath.toFile(), database);
    }
}
