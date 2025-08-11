package com.adaptivescale.rosetta.cli.services;

import com.adaptivescale.rosetta.cli.outputs.YamlModelOutput;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class RosettaService {
    public static final String DEFAULT_MODEL_YAML = "model.yaml";

    public void extractModel(Connection connection, Path sourceWorkspace) throws Exception {
        extractModel(connection, sourceWorkspace, false);
    }

    public void extractModel(Connection connection, Path sourceWorkspace, boolean forceDelete) throws Exception {
        Path modelYamlPath = sourceWorkspace.resolve(DEFAULT_MODEL_YAML);

        if (forceDelete) {
            if (Files.exists(sourceWorkspace)) {
                FileUtils.deleteDirectory(sourceWorkspace.toFile());
            }
            Files.createDirectories(sourceWorkspace);
        } else {
            if (Files.exists(modelYamlPath)) {
                log.info("model.yaml already exists at {}. Skipping extraction.", modelYamlPath);
                return;
            }
            if (!Files.exists(sourceWorkspace)) {
                Files.createDirectories(sourceWorkspace);
            }
        }

        Database result = SourceGeneratorFactory.sourceGenerator(connection).generate(connection);
        YamlModelOutput yamlInputModel = new YamlModelOutput(DEFAULT_MODEL_YAML, sourceWorkspace);
        yamlInputModel.write(result);
        log.info("Successfully written input database yaml ({}).", yamlInputModel.getFilePath());
    }
}
