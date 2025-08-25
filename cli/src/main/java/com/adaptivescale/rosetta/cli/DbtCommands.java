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

@CommandLine.Command(name = "dbt-next", description = "Commands for dbt model handling", subcommandsRepeatable = true)
public class DbtCommands {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ParentCommand
    private Cli parent;

    private final DbtModelService dbtModelService = new DbtModelService();

    private String sourceName;
    private Connection connection;
    private Path sourceWorkspace;
    private List<String> resolvedInputPaths;
    private String resolvedOutputPath;

    @CommandLine.Command(name = "extract", description = "Generate dbt YAML models from connection config")
    public void extract(
            @CommandLine.Option(names = {"-s", "--source"}, required = true)
            String sourceName,
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path. If not specified, uses default extract path.")
            String outputPath
    ) throws Exception {
        initializeGlobals(sourceName, null, outputPath, CommandType.EXTRACT);
        dbtModelService.generateDBTYamlModels(connection, sourceWorkspace, resolvedOutputPath);
    }

    @CommandLine.Command(name = "staging", description = "Generate staging dbt models from connection config")
    public void staging(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
            @CommandLine.Option(names = {"-i", "--input"}, description = "Input files or folders (YAML/YML files only). If not specified, uses default source workspace.")
            List<String> inputPaths,
            @CommandLine.Option(names = {"-o", "--output"}, description = "Output directory path. If not specified, uses default staging layer path.")
            String outputPath
    ) throws Exception {
        initializeGlobals(sourceName, inputPaths, outputPath, CommandType.STAGING);
        dbtModelService.generateStagingModels(connection, sourceWorkspace, resolvedInputPaths, resolvedOutputPath);
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
        initializeGlobals(sourceName, inputPaths, outputPath, CommandType.INCREMENTAL);

        if (resolvedInputPaths.isEmpty()) {
            log.warn("No valid input sources found. Please either:");
            log.warn("  1. Run `dbt staging` first to generate staging models, or");
            log.warn("  2. Specify input files with --input, or");
            log.warn("  3. Ensure YAML model files exist in dbt/models/ directory");
            return;
        }

        List<String> yamlFiles = resolvedInputPaths.stream()
                .filter(path -> path.toLowerCase().endsWith(".yaml") || path.toLowerCase().endsWith(".yml"))
                .collect(Collectors.toList());

        List<String> sqlFiles = resolvedInputPaths.stream()
                .filter(path -> path.toLowerCase().endsWith(".sql"))
                .collect(Collectors.toList());

        if (!yamlFiles.isEmpty()) {
            log.info("Processing {} YAML files for enhanced model generation", yamlFiles.size());
            dbtModelService.generateEnhancedModelsFromYaml(connection, sourceWorkspace, yamlFiles, resolvedOutputPath, prefix);
        }

        if (!sqlFiles.isEmpty()) {
            log.info("Processing {} SQL files for enhanced model generation", sqlFiles.size());
            dbtModelService.generateEnhancedModels(connection, sourceWorkspace, sqlFiles, resolvedOutputPath, prefix);
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
        initializeGlobals(sourceName, inputPaths, outputPath, CommandType.BUSINESS);

        dbtModelService.generateBusinessModels(
                connection,
                sourceWorkspace,
                parent.getConfig().getOpenAIApiKey(),
                parent.getConfig().getOpenAIModel(),
                userPrompt,
                resolvedInputPaths,
                resolvedOutputPath
        );
    }

    /**
     * Initialize global variables with defaults based on command and user inputs
     */
    private void initializeGlobals(String sourceName, List<String> userInputPaths, String userOutputPath, CommandType commandType) throws Exception {
        requireConfig(parent.getConfig());

        this.sourceName = sourceName;
        this.connection = getConnection(parent.getConfig(), sourceName);
        this.sourceWorkspace = Paths.get(".", sourceName);

        if (commandType != CommandType.EXTRACT) {
            validateSourceWorkspace();
        }

        this.resolvedInputPaths = resolveInputPaths(userInputPaths, commandType);

        this.resolvedOutputPath = resolveOutputPath(userOutputPath, commandType);

        logResolvedPaths(commandType);
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

    private enum CommandType {
        STAGING, INCREMENTAL, BUSINESS, EXTRACT
    }

    private void validateSourceWorkspace() {
        if (!Files.isDirectory(sourceWorkspace)) {
            throw new RuntimeException(String.format("Cannot find directory: %s for source name: %s", sourceWorkspace, sourceName));
        }
    }

    private List<String> resolveInputPaths(List<String> userInputPaths, CommandType commandType) throws IOException {
        if (userInputPaths != null && !userInputPaths.isEmpty()) {
            validateInputPaths(userInputPaths, getAllowedExtensions(commandType));
            return userInputPaths;
        }

        return getDefaultInputPaths(commandType);
    }

    private String resolveOutputPath(String userOutputPath, CommandType commandType) {
        if (userOutputPath != null && !userOutputPath.isEmpty()) {
            validateOutputPath(userOutputPath);
            return userOutputPath;
        }

        return getDefaultOutputPath(commandType);
    }

    private List<String> getDefaultInputPaths(CommandType commandType) throws IOException {
        if (Objects.requireNonNull(commandType) == CommandType.INCREMENTAL) {
            return getIncrementalDefaultInputs();
        }
        return null;
    }

    private List<String> getIncrementalDefaultInputs() throws IOException {
        Path stagingPath = sourceWorkspace.resolve("dbt").resolve("models").resolve(DbtModelService.STAGING_LAYER);

        if (Files.exists(stagingPath)) {
            List<Path> stagingSqlFiles = dbtModelService.listSqlFiles(stagingPath);
            if (!stagingSqlFiles.isEmpty()) {
                log.info("Using default: Found {} SQL files in staging layer", stagingSqlFiles.size());
                return stagingSqlFiles.stream().map(Path::toString).collect(Collectors.toList());
            }
        }

        Path dbtModelsPath = sourceWorkspace.resolve("dbt").resolve("models");
        if (Files.exists(dbtModelsPath)) {
            try (Stream<Path> paths = Files.walk(dbtModelsPath)) {
                List<String> yamlFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
                        })
                        .map(Path::toString)
                        .collect(Collectors.toList());

                if (!yamlFiles.isEmpty()) {
                    log.info("Using default: Found {} YAML files in dbt/models/", yamlFiles.size());
                    return yamlFiles;
                }
            } catch (IOException e) {
                log.error("Error scanning dbt/models/ directory for YAML files", e);
            }
        }

        log.warn("No default input sources found for incremental command");
        return new ArrayList<>();
    }


    private String getDefaultOutputPath(CommandType commandType) {
        Path dbtModelsPath = sourceWorkspace.resolve("dbt").resolve("models");

        switch (commandType) {
            case STAGING:
                return dbtModelsPath.resolve(DbtModelService.STAGING_LAYER).toString();

            case INCREMENTAL:
                return dbtModelsPath.resolve("incremental").toString();

            case BUSINESS:
                return dbtModelsPath.resolve("business").toString();

            case EXTRACT:
                return dbtModelsPath.toString();

            default:
                return dbtModelsPath.toString();
        }
    }

    private List<String> getAllowedExtensions(CommandType commandType) {
        switch (commandType) {
            case STAGING:
                return Arrays.asList("yaml", "yml");
            case INCREMENTAL:
            case BUSINESS:
                return Arrays.asList("sql", "yaml", "yml");
            default:
                return Arrays.asList("sql", "yaml", "yml");
        }
    }

    private void logResolvedPaths(CommandType commandType) {
        log.info("=== {} Command Configuration ===", commandType.name());
        log.info("Source: {}", sourceName);
        log.info("Workspace: {}", sourceWorkspace);
        if (resolvedInputPaths != null && !resolvedInputPaths.isEmpty()) {
            log.info("Input paths: {}", resolvedInputPaths);
        } else {
            log.info("Input paths: Using service defaults");
        }
        log.info("Output path: {}", resolvedOutputPath);
        log.info("=====================================");
    }
}