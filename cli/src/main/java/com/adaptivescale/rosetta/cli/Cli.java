package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.cli.outputs.StringOutput;
import com.adaptivescale.rosetta.cli.outputs.YamlModelOutput;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.translator.Translator;
import com.adaptivescale.rosetta.translator.TranslatorFactory;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adaptivescale.rosetta.cli.Constants.*;

@Slf4j
@CommandLine.Command(name = "cli", mixinStandardHelpOptions = true, version = "rosetta 0.0.1",
        description = "Declarative Database Management - DDL Transpiler"
)
class Cli implements Callable<Void> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Option(names = {"-c", "--config"},
            converter = ConfigYmlConverter.class,
            defaultValue = CONFIG_NAME,
            description = "YAML config file. If none is supplied it will use main.conf in the current directory if it exists.")
    private Config config;

    @Override
    public Void call() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    @CommandLine.Command(name = "extract", description = "Extract schema chosen from connection config.", mixinStandardHelpOptions = true)
    private void extract(@CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
                         @CommandLine.Option(names = {"-t", "--convert-to"}) String targetName
    ) throws Exception {
        requireConfig(config);
        Optional<Connection> source = config.getConnection(sourceName);
        if (source.isEmpty()) {
            throw new RuntimeException("Can not find source with name: " + sourceName + " configured in config.");
        }

        Path sourceWorkspace = Paths.get("./", sourceName);
        FileUtils.deleteDirectory(sourceWorkspace.toFile());
        Files.createDirectory(sourceWorkspace);

        Database result = SourceGeneratorFactory.sourceGenerator(source.get()).generate(source.get());
        YamlModelOutput yamlInputModel = new YamlModelOutput("model.yaml", sourceWorkspace);
        yamlInputModel.write(result);
        log.info("Successfully written input database yaml ({}).", yamlInputModel.getFilePath());

        if (Optional.ofNullable(targetName).isEmpty()) {
            return;
        }

        Optional<Connection> target = config.getConnection(targetName);
        if (target.isEmpty()) {
            throw new RuntimeException("Can not find target with name: " + Optional.of(targetName).orElse(null) + " configured in config.");
        }

        // no need to read source workspace because we already have model to be translated
        Translator<Database, Database> translator = TranslatorFactory.translator(source.get().getDbType(),
                target.get().getDbType());
        result = translator.translate(result);

        Path targetWorkspace = Paths.get("./", targetName);
        FileUtils.deleteDirectory(targetWorkspace.toFile());
        Files.createDirectory(targetWorkspace);

        YamlModelOutput yamlOutputModel = new YamlModelOutput("model.yaml", targetWorkspace);
        yamlOutputModel.write(result);
        log.info("Successfully written output database yaml ({}).", yamlOutputModel.getFilePath());
    }

    @CommandLine.Command(name = "compile", description = "Generate DDL for target Database [bigquery, snowflake, ???]", mixinStandardHelpOptions = true)
    private void compile(@CommandLine.Option(names = {"-s", "--source"}) String sourceName,
                         @CommandLine.Option(names = {"-t", "--target"}, required = true) String targetName
    ) throws Exception {
        requireConfig(config);

        Optional<Connection> target = config.getConnection(targetName);
        if (target.isEmpty()) {
            throw new RuntimeException("Can not find target with name: " + Optional.ofNullable(targetName).orElse(null) + " configured in config.");
        }

        Path targetWorkspace = Paths.get("./", targetName);
        List<FileNameAndDatabasePair> translatedModels;

        if (null == sourceName || sourceName.isBlank()) {
            if (!Files.isDirectory(targetWorkspace)) {
                throw new RuntimeException(String.format("Can not find directory: %s for target name: %s to find models" +
                        " for ddl generation", targetWorkspace, targetName));
            }
            translatedModels = getDatabases(targetWorkspace).collect(Collectors.toList());

            if (translatedModels.size() < 1) {
                throw new RuntimeException("Can not find any file with extension : .yaml." +
                        " Use extract command to generate models.");
            }
        } else {
            Optional<Connection> source = config.getConnection(sourceName);
            if (source.isEmpty()) {
                throw new RuntimeException(String.format("Can not find source with name: %s configured in config.",
                        sourceName));
            }
            Path sourceWorkspace = Paths.get("./", sourceName);

            if (!Files.isDirectory(sourceWorkspace)) {
                throw new RuntimeException(String.format("Can not find directory: %s for source name: %s to find" +
                        " models for translation", sourceWorkspace, sourceName));
            }

            FileUtils.deleteDirectory(targetWorkspace.toFile());
            Files.createDirectory(targetWorkspace);

            Translator<Database, Database> translator = TranslatorFactory
                    .translator(source.get().getDbType(), target.get().getDbType());

            translatedModels = getDatabases(sourceWorkspace)
                    .map(translateDatabases(translator))
                    .collect(Collectors.toList());

            translatedModels.forEach(writeOutput(targetWorkspace));
        }

        String ddl = translatedModels.stream().map(stringDatabaseEntry -> {
            DDL modelDDL = DDLFactory.ddlForDatabaseType(stringDatabaseEntry.getValue().getDatabaseType());
            return modelDDL.createDataBase(stringDatabaseEntry.getValue());
        }).reduce("", (s, s2) -> s.concat("\n\n\n").stripLeading().concat(s2));

        StringOutput stringOutput = new StringOutput("ddl.sql", targetWorkspace);
        stringOutput.write(ddl);
        log.info("Successfully written ddl ({}).", stringOutput.getFilePath());
    }

    @CommandLine.Command(name = "init", description = "Creates a sample config (main.conf) and model directory.", mixinStandardHelpOptions = true)
    private void init(@CommandLine.Parameters(index = "0", description = "Project name.", defaultValue = "")
                              String projectName) throws IOException {
        Path fileName = Paths.get(projectName, CONFIG_NAME);
        InputStream resourceAsStream = getClass().getResourceAsStream("/" + TEMPLATE_CONFIG_NAME);
        Path projectDirectory = Path.of(projectName);
        if (!projectName.isEmpty() && Files.isDirectory(projectDirectory)) {
            throw new RuntimeException(String.format("Project (%s) already exists.", projectName));
        }
        if (Files.exists(fileName)) {
            throw new RuntimeException("A configuration for this directory already exists.");
        }
        Files.createDirectories(projectDirectory);
        Files.copy(resourceAsStream, fileName);
        log.info("Successfully created project with a sample config ({}).", fileName);
        if (!projectName.isEmpty()) {
            log.info("In order to start using the newly created project please change your working directory.");
        }
    }

    private void requireConfig(Config config) {
        if (config == null) {
            throw new RuntimeException("Config file is required.");
        }
    }

    /**
     * @param directory directory same as connection name
     * @return consumer
     */
    private Consumer<AbstractMap.SimpleImmutableEntry<String, Database>> writeOutput(Path directory) {
        return fileNameAndDatabasePair -> {
            try {
                new YamlModelOutput(fileNameAndDatabasePair.getKey(), directory)
                        .write(fileNameAndDatabasePair.getValue());
            } catch (Exception e) {
                throw new RuntimeException(String.format("Unable to write translated models to path: %s", directory), e);
            }
        };
    }

    /**
     * Find every file that ends with .yaml
     *
     * @param directory where to search
     * @return Stream
     * @throws IOException exception with io
     */
    private Stream<FileNameAndDatabasePair> getDatabases(Path directory) throws IOException {
        return Files.list(directory)
                .filter(path -> !Files.isDirectory(path) && "yaml".equals(FilenameUtils.getExtension(path.toString())))
                .map(path -> {
                    try {
                        Database input = new ObjectMapper(new YAMLFactory()).readValue(path.toFile(), Database.class);
                        return new FileNameAndDatabasePair(path.getFileName().toString(), input);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
    }

    private Function<FileNameAndDatabasePair, FileNameAndDatabasePair> translateDatabases(Translator<Database, Database> translator) {
        return fileNameAndModelPair -> {
            try {
                Database translated = translator.translate(fileNameAndModelPair.getValue());
                return new FileNameAndDatabasePair(fileNameAndModelPair.getKey(), translated);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        };
    }

    /**
     * Simple KeyValue Pair where key is file name and value is Database
     */
    private final static class FileNameAndDatabasePair extends AbstractMap.SimpleImmutableEntry<String, Database> {
        public FileNameAndDatabasePair(String key, Database value) {
            super(key, value);
        }
    }
}
