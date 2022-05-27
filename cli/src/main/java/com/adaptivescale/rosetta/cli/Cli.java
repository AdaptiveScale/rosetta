package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.translator.Translator;
import com.adaptivescale.rosetta.translator.TranslatorFactory;
import com.adataptivescale.rosetta.source.core.ColumnsExtractor;
import com.adataptivescale.rosetta.source.core.DefaultGenerator;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import com.adataptivescale.rosetta.source.core.TablesExtractor;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

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
                         @CommandLine.Option(names = {"-o", "--output-dir"}) Optional<Path> outputDirectory
    ) throws Exception {
        Optional<Target> source = config.getTarget(sourceName);
        if (!source.isPresent()) {
            throw new RuntimeException("Can not find source with name: " + sourceName + " configured in config.");
        }

        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnsExtractor columnsExtractor = new ColumnsExtractor(source.get());
        Database result = new DefaultGenerator(tablesExtractor, columnsExtractor).generate(source.get());

        if (targetName.isPresent()) {

            Optional<Target> target = config.getTarget(targetName.get());
            if (!target.isPresent()) {
                throw new RuntimeException("Can not find target with name: " + targetName.orElseGet(() -> null) + " configured in config.");
            }

            Translator<Database, Database> translator = TranslatorFactory.translator(source.get().getDbType(),
                    target.get().getDbType());
            result = translator.translate(result);
        }

        Output output = outputDirectory.isPresent() ? new DirectoryOutput(sourceName, targetName.get(), outputDirectory.get()) : new ConsoleOutput();
        output.write(result);
    }

    @CommandLine.Command(name = "compile", description = "Generate DDL for target Database [bigquery, snowflake, …]")
    private void compile(@CommandLine.Option(names = {"-t", "--target"}) String targetName) throws Exception {
        Optional<Target> target = config.getTarget(targetName);
        if (!target.isPresent()) {
            throw new RuntimeException("Can not find target with name: " + targetName + " configured in config.");
        }
        Database database = SourceGeneratorFactory.sourceGenerator(target.get()).generate(target.get());
        DDL ddl = DDLFactory.ddlForDatabaseType(target.get().getDbType());
        ddl.createDataBase(database);
        new ConsoleOutput().write(database);
    }
}
