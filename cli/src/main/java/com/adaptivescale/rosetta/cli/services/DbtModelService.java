package com.adaptivescale.rosetta.cli.services;

import com.adaptivescale.rosetta.cli.outputs.DbtSqlModelOutput;
import com.adaptivescale.rosetta.cli.outputs.DbtYamlModelOutput;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.dbt.DbtModelGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queryhelper.pojo.GenericResponse;
import queryhelper.service.DbtAIService;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adaptivescale.rosetta.cli.helpers.DbtEnhancedModelTransformer.enhancedSQLGenerator;

public class DbtModelService {

    private static final Logger log = LoggerFactory.getLogger(DbtModelService.class);

    public static final String STAGING_LAYER = "staging";
    public static final String ENHANCED_LAYER = "enhanced";
    public static final String BUSINESS_LAYER = "business";
    public static final String RAW_LAYER = "raw";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public void generateStagingModels(Connection connection, Path sourceWorkspace) throws IOException {
        Path stagingPath = sourceWorkspace.resolve("dbt").resolve("models").resolve(STAGING_LAYER);
        Files.createDirectories(stagingPath);

        List<Database> databases = readYamlModels(sourceWorkspace);
        DbtModel dbtModel = DbtModelGenerator.dbtModelGenerator(databases);

        new DbtYamlModelOutput(stagingPath).write(dbtModel);
        Map<String, String> sqlModels = DbtModelGenerator.dbtSQLGenerator(dbtModel, false);
        new DbtSqlModelOutput(stagingPath).write(sqlModels);

        log.info("Written staging DBT models to {}", stagingPath);
    }

    public void generateEnhancedModels(Connection connection, Path sourceWorkspace, List<Path> stagingSqlFiles) throws IOException {
        Path enhancedPath = sourceWorkspace.resolve("dbt").resolve("models").resolve(ENHANCED_LAYER);
        Files.createDirectories(enhancedPath);

        List<Database> databases = readYamlModels(sourceWorkspace);
        DbtModel dbtModel = DbtModelGenerator.dbtModelGenerator(databases);
        Map<String, String> enhancedSql = enhancedSQLGenerator(stagingSqlFiles, dbtModel);
        new DbtSqlModelOutput(enhancedPath).write(enhancedSql);

        log.info("Written enhanced DBT models to {}", enhancedPath);
    }

    public void generateBusinessModels(Connection connection, Path sourceWorkspace, String apiKey, String model, String userPrompt) throws IOException {
        Path dbtModels = sourceWorkspace.resolve("dbt").resolve("models");
        String bestLayer = findBestAvailableLayer(dbtModels, sourceWorkspace);
        List<String> sources = readModelContents(dbtModels, bestLayer, sourceWorkspace);

        if (sources.isEmpty()) {
            throw new RuntimeException("No valid model content found in any layer.");
        }

        Path outputPath = dbtModels.resolve(BUSINESS_LAYER);
        Files.createDirectories(outputPath);

        GenericResponse response = DbtAIService.generateBusinessModels(
                apiKey, model, outputPath, String.join("\n\n", sources), userPrompt
        );

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to generate business models: " + response.getMessage());
        }

        log.info("Generated business models into {}", outputPath);
    }

    public List<Database> readYamlModels(Path directory) throws IOException {
        return Files.list(directory)
                .filter(path -> FilenameUtils.getExtension(path.toString()).equalsIgnoreCase("yaml"))
                .map(path -> {
                    try {
                        return yamlMapper.readValue(path.toFile(), Database.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse " + path, e);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Path> listSqlFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(path -> path.toString().endsWith(".sql"))
                    .collect(Collectors.toList());
        }
    }

    private String findBestAvailableLayer(Path dbtPath, Path sourcePath) {
        if (layerExists(dbtPath.resolve(ENHANCED_LAYER))) return ENHANCED_LAYER;
        if (layerExists(dbtPath.resolve(STAGING_LAYER))) return STAGING_LAYER;
        if (Files.exists(sourcePath.resolve("model.yaml"))) return RAW_LAYER;
        throw new RuntimeException("No valid model layer found (raw, staging, or enhanced).");
    }

    private boolean layerExists(Path path) {
        try (Stream<Path> stream = Files.exists(path) ? Files.list(path) : Stream.empty()) {
            return stream.anyMatch(p -> p.toString().endsWith(".sql"));
        } catch (IOException e) {
            return false;
        }
    }

    private List<String> readModelContents(Path dbtPath, String layer, Path sourcePath) throws IOException {
        List<String> result = new ArrayList<>();
        switch (layer) {
            case RAW_LAYER:
                Path rawModel = sourcePath.resolve("model.yaml");
                result.add("model.yaml\n" + Files.readString(rawModel));
                break;
            case STAGING_LAYER:
            case ENHANCED_LAYER:
                Path modelDir = dbtPath.resolve(layer);
                Files.walk(modelDir)
                        .filter(path -> path.toString().endsWith(".sql"))
                        .forEach(path -> {
                            try {
                                result.add(path.getFileName() + "\n" + Files.readString(path));
                            } catch (IOException e) {
                                log.error("Failed to read model file: {}", path, e);
                            }
                        });
                break;
            default:
                throw new IllegalArgumentException("Unknown layer: " + layer);
        }
        return result;
    }
}
