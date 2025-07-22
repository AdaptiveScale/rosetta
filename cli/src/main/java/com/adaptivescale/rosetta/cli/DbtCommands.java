package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.services.DbtModelService;
import com.adaptivescale.rosetta.common.models.input.Connection;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adaptivescale.rosetta.cli.helpers.CliHelper.getConnection;
import static com.adaptivescale.rosetta.cli.helpers.CliHelper.requireConfig;

@CommandLine.Command(name = "new_dbt", description = "Commands for dbt model handling", subcommandsRepeatable = true)
public class DbtCommands {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ParentCommand
    private Cli parent;

    private final DbtModelService dbtModelService = new DbtModelService();

    @CommandLine.Command(name = "staging", description = "Generate staging dbt models from connection config")
    public void staging(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
            @CommandLine.Option(names = {"-i", "--input"}, description = "Input files or folders (YAML/YML files only). If not specified, uses default source workspace.")
            List<String> inputPaths,
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path. If not specified, uses default staging layer path.")
            String outputPath
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        if (!Files.isDirectory(sourceWorkspace)) {
            throw new RuntimeException(String.format("Cannot find directory: %s for source name: %s to find models", sourceWorkspace, sourceName));
        }

        if (inputPaths != null && !inputPaths.isEmpty()) {
            log.info("Using specified input paths for staging: {}", inputPaths);
            validateInputPaths(inputPaths, Arrays.asList("yaml", "yml"));
        }

        if (outputPath != null && !outputPath.isEmpty()) {
            log.info("Using specified output path for staging: {}", outputPath);
            validateOutputPath(outputPath);
        }

        dbtModelService.generateStagingModels(connection, sourceWorkspace, inputPaths, outputPath);
    }

    @CommandLine.Command(name = "incremental", description = "Generate enhanced dbt models from connection config")
    public void incremental(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
            @CommandLine.Option(names = {"-i", "--input"}, description = "Input files or folders (SQL or YAML files). If not specified, uses staging layer.")
            List<String> inputPaths,
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path. If not specified, uses default enhanced layer path.")
            String outputPath,
            @CommandLine.Option(names = {"--prefix"}, description = "Prefix for generated sql files") String prefix
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        if (inputPaths != null && !inputPaths.isEmpty()) {
            log.info("Using specified input paths for incremental: {}", inputPaths);

            // Separate SQL and YAML files
            List<String> yamlFiles = inputPaths.stream()
                    .filter(path -> path.toLowerCase().endsWith(".yaml") || path.toLowerCase().endsWith(".yml"))
                    .collect(Collectors.toList());

            List<String> sqlFiles = inputPaths.stream()
                    .filter(path -> path.toLowerCase().endsWith(".sql"))
                    .collect(Collectors.toList());

            // Validate all input paths
            validateInputPaths(inputPaths, Arrays.asList("sql", "yaml", "yml"));

            if (outputPath != null && !outputPath.isEmpty()) {
                log.info("Using specified output path for incremental: {}", outputPath);
                validateOutputPath(outputPath);
            }

            // Process YAML files if present
            if (!yamlFiles.isEmpty()) {
                log.info("Processing {} YAML files for enhanced model generation", yamlFiles.size());
                dbtModelService.generateEnhancedModelsFromYaml(connection, sourceWorkspace, yamlFiles, outputPath, prefix);
            }

            // Process SQL files if present
            if (!sqlFiles.isEmpty()) {
                log.info("Processing {} SQL files for enhanced model generation", sqlFiles.size());
                dbtModelService.generateEnhancedModels(connection, sourceWorkspace, sqlFiles, outputPath, prefix);
            }

            if (yamlFiles.isEmpty() && sqlFiles.isEmpty()) {
                log.warn("No valid SQL or YAML files found in the specified input paths.");
                return;
            }

        } else {
            // No input specified - check staging first, then fallback to YAML files
            Path stagingPath = sourceWorkspace.resolve("dbt").resolve("models").resolve(DbtModelService.STAGING_LAYER);

            if (outputPath != null && !outputPath.isEmpty()) {
                log.info("Using specified output path for incremental: {}", outputPath);
                validateOutputPath(outputPath);
            }

            // First priority: Check for SQL files in staging
            if (Files.exists(stagingPath)) {
                List<Path> stagingSqlFiles = dbtModelService.listSqlFiles(stagingPath);
                if (!stagingSqlFiles.isEmpty()) {
                    log.info("Found {} SQL files in staging layer. Generating enhanced models from staging.", stagingSqlFiles.size());
                    dbtModelService.generateEnhancedModels(connection, sourceWorkspace, null, outputPath, prefix);
                    return;
                } else {
                    log.info("No SQL files found in staging layer.");
                }
            } else {
                log.info("Staging directory does not exist: {}", stagingPath);
            }

            // Second priority: Check for YAML files in dbt/models/
            Path dbtModelsPath = sourceWorkspace.resolve("dbt").resolve("models");
            if (Files.exists(dbtModelsPath)) {
                try (Stream<Path> paths = Files.walk(dbtModelsPath)) {
                    List<Path> yamlFiles = paths
                            .filter(Files::isRegularFile)
                            .filter(path -> {
                                String fileName = path.getFileName().toString().toLowerCase();
                                return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
                            })
                            .collect(Collectors.toList());

                    if (!yamlFiles.isEmpty()) {
                        log.info("Found {} YAML files in dbt/models/. Generating enhanced models from YAML files.", yamlFiles.size());
                        dbtModelService.generateEnhancedModelsFromYaml(connection, sourceWorkspace, null, outputPath, prefix);
                        return;
                    } else {
                        log.info("No YAML files found in dbt/models/ directory.");
                    }
                } catch (IOException e) {
                    log.error("Error scanning dbt/models/ directory for YAML files", e);
                    throw e;
                }
            } else {
                log.info("dbt/models/ directory does not exist: {}", dbtModelsPath);
            }

            // No valid sources found
            log.warn("No valid input sources found. Please either:");
            log.warn("  1. Run `dbt staging` first to generate staging models, or");
            log.warn("  2. Specify input files with --input, or");
            log.warn("  3. Ensure YAML model files exist in dbt/models/ directory");
        }
    }

    @CommandLine.Command(name = "business", description = "Generate business dbt models from connection config")
    public void business(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
            @CommandLine.Option(names = {"-q", "--query"}, required = false) String userPrompt,
            @CommandLine.Option(names = {"-i", "--input"}, description = "Input files or folders (SQL or YAML files). If not specified, uses best available layer.")
            List<String> inputPaths,
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path. If not specified, uses default business layer path.")
            String outputPath
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        if (inputPaths != null && !inputPaths.isEmpty()) {
            log.info("Using specified input paths for business: {}", inputPaths);
            validateInputPaths(inputPaths, Arrays.asList("sql", "yaml", "yml"));
        }

        if (outputPath != null && !outputPath.isEmpty()) {
            log.info("Using specified output path for business: {}", outputPath);
            validateOutputPath(outputPath);
        }

        dbtModelService.generateBusinessModels(connection, sourceWorkspace,
                parent.getConfig().getOpenAIApiKey(),
                parent.getConfig().getOpenAIModel(),
                userPrompt,
                inputPaths,
                outputPath
        );
    }

    @CommandLine.Command(name = "extract", description = "Generate dbt YAML models from connection config")
    public void extract(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        if (!Files.isDirectory(sourceWorkspace)) {
            throw new RuntimeException(String.format("Cannot find directory: %s for source name: %s to find models", sourceWorkspace, sourceName));
        }

        dbtModelService.generateDBTYamlModels(connection, sourceWorkspace);
    }

    /**
     * Validate that input paths exist and contain files with allowed extensions
     */
    private void validateInputPaths(List<String> inputPaths, List<String> allowedExtensions) {
        for (String inputPath : inputPaths) {
            Path path = Paths.get(inputPath);

            if (!Files.exists(path)) {
                throw new RuntimeException("Input path does not exist: " + inputPath);
            }

            if (Files.isRegularFile(path)) {
                String extension = getFileExtension(path.toString());
                if (!allowedExtensions.stream().anyMatch(ext -> ext.equalsIgnoreCase(extension))) {
                    throw new RuntimeException(String.format(
                            "File %s has unsupported extension '%s'. Allowed extensions: %s",
                            inputPath, extension, allowedExtensions));
                }
            }
        }
    }

    private void validateOutputPath(String outputPath) {
        Path path = Paths.get(outputPath);

        // Check if parent directory exists or can be created
        Path parentPath = path.getParent();
        if (parentPath != null && !Files.exists(parentPath)) {
            try {
                Files.createDirectories(parentPath);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create parent directories for output path: " + outputPath, e);
            }
        }
        if (Files.exists(path) && Files.isRegularFile(path)) {
            throw new RuntimeException("Output path must be a directory, not a file: " + outputPath);
        }
    }


    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
}