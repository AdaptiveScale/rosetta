package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.cli.outputs.StringOutput;
import com.adaptivescale.rosetta.cli.outputs.YamlModelOutput;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.translator.Translator;
import com.adaptivescale.rosetta.translator.TranslatorFactory;
import com.adataptivescale.rosetta.source.core.ColumnsExtractor;
import com.adataptivescale.rosetta.source.core.DefaultGenerator;
import com.adataptivescale.rosetta.source.core.TablesExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.adaptivescale.rosetta.cli.Constants.*;

@CommandLine.Command(name = "cli", mixinStandardHelpOptions = true, version = "checksum 4.0",
        description = "todo"
)
class Cli implements Callable<Void> {

    @CommandLine.Option(names = {"-i", "--input-config"},
            converter = ConfigYmlConverter.class,
            required = true,
            defaultValue = "main.yaml")
    private Config config;

    @Override
    public Void call() {
        return null;
    }

    @CommandLine.Command(name = "extract")
    private void extract(@CommandLine.Option(names = {"-s", "--source"}) String sourceName,
                         @CommandLine.Option(names = {"-t", "--convert-to"}) Optional<String> targetName,
                         @CommandLine.Option(names = {"-o", "--output-dir"}, defaultValue = "./") Optional<Path> outputDirectory
    ) throws Exception {
        Optional<Target> source = config.getTarget(sourceName);
        if (!source.isPresent()) {
            throw new RuntimeException("Can not find source with name: " + sourceName + " configured in config.");
        }

        if (!outputDirectory.isPresent()) {
            throw new RuntimeException("Output directory is null");
        }

        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnsExtractor columnsExtractor = new ColumnsExtractor(source.get());
        Database result = new DefaultGenerator(tablesExtractor, columnsExtractor).generate(source.get());

        new YamlModelOutput(MODEL_INPUT_NAME, outputDirectory.get()).write(result);


        if (!targetName.isPresent()) {
            return;
        }

        Optional<Target> target = config.getTarget(targetName.get());
        if (!target.isPresent()) {
            throw new RuntimeException("Can not find target with name: " + targetName.orElse(null) + " configured in config.");
        }

        Translator<Database, Database> translator = TranslatorFactory.translator(source.get().getDbType(),
                target.get().getDbType());
        result = translator.translate(result);

        new YamlModelOutput(MODEL_OUTPUT_NAME, outputDirectory.get()).write(result);
    }

    @CommandLine.Command(name = "compile", description = "Generate DDL for target Database [bigquery, snowflake, …]")
    private void compile(@CommandLine.Option(names = {"-t", "--target"}) String targetName,
                         @CommandLine.Option(names = {"-i", "--input-dir"}, defaultValue = "./") Optional<Path> inputDirectory) throws Exception {
        Optional<Target> target = config.getTarget(targetName);
        if (!target.isPresent()) {
            throw new RuntimeException("Can not find target with name: " + targetName + " configured in config.");
        }

        if (!inputDirectory.isPresent()) {
            throw new RuntimeException("Input directory is null");
        }

        Path modelDirectory = inputDirectory.get().resolve(MODEL_DIRECTORY_NAME);
        if (!Files.exists(modelDirectory)) {
            throw new RuntimeException("Can not find model directory");
        }

        Path inputDatabasePath = modelDirectory.resolve(MODEL_INPUT_NAME);
        if (!Files.exists(inputDatabasePath)) {
            throw new RuntimeException("Can not locate " + MODEL_INPUT_NAME + " in directory: " + modelDirectory.toAbsolutePath());
        }

        Database input = new ObjectMapper(new YAMLFactory()).readValue(inputDatabasePath.toFile(), Database.class);
        Translator<Database, Database> translator = TranslatorFactory.translator(input.getDatabaseType(), target.get().getDbType());
        Database translatedInput = translator.translate(input);

        new YamlModelOutput(MODEL_OUTPUT_NAME, inputDirectory.get()).write(translatedInput);

        DDL ddl = DDLFactory.ddlForDatabaseType(target.get().getDbType());
        String ddlDataBase = ddl.createDataBase(translatedInput);
        new StringOutput("ddl.sql", modelDirectory).write(ddlDataBase);
    }
}
