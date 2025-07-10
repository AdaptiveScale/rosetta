package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.services.DbtModelService;
import com.adaptivescale.rosetta.common.models.input.Connection;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.adaptivescale.rosetta.cli.helpers.CliHelper.getConnection;
import static com.adaptivescale.rosetta.cli.helpers.CliHelper.requireConfig;

@CommandLine.Command(name = "dbt", description = "Commands for dbt model handling", subcommandsRepeatable = true)
public class DbtCommands {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.ParentCommand
    private Cli parent;

    private final DbtModelService dbtModelService = new DbtModelService();

    @CommandLine.Command(name = "staging", description = "Generate staging dbt models from connection config")
    public void staging(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        if (!Files.isDirectory(sourceWorkspace)) {
            throw new RuntimeException(String.format("Cannot find directory: %s for source name: %s to find models", sourceWorkspace, sourceName));
        }

        dbtModelService.generateStagingModels(connection, sourceWorkspace);
    }

    @CommandLine.Command(name = "incremental", description = "Generate enhanced dbt models from connection config")
    public void incremental(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);

        Path stagingPath = sourceWorkspace.resolve("dbt").resolve("models").resolve(DbtModelService.STAGING_LAYER);
        if (!Files.exists(stagingPath)) {
            log.info("Staging models not found. Run `dbt staging` first.");
            return;
        }

        List<Path> stagingSqlFiles = dbtModelService.listSqlFiles(stagingPath);
        if (stagingSqlFiles.isEmpty()) {
            log.info("No .sql models found in staging layer.");
            return;
        }

        dbtModelService.generateEnhancedModels(connection, sourceWorkspace, stagingSqlFiles);
    }

    @CommandLine.Command(name = "business", description = "Generate business dbt models from connection config")
    public void business(
            @CommandLine.Option(names = {"-s", "--source"}, required = true) String sourceName,
            @CommandLine.Option(names = {"-q", "--query"}, required = false) String userPrompt
    ) throws Exception {
        requireConfig(parent.getConfig());
        Connection connection = getConnection(parent.getConfig(), sourceName);
        Path sourceWorkspace = Paths.get("./", sourceName);
        dbtModelService.generateBusinessModels(connection, sourceWorkspace,
                parent.getConfig().getOpenAIApiKey(),
                parent.getConfig().getOpenAIModel(),
                userPrompt
        );
    }
}
