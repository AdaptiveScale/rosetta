package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.ViewExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DefaultGenerator implements Generator<Database, Connection> {
    private final TableExtractor tableExtractor;
    private final ViewExtractor viewExtractor;
    private final ColumnExtractor columnsExtractor;
    private final JDBCDriverProvider driverProvider;

    DefaultGenerator(TableExtractor tableExtractor, ViewExtractor viewExtractor,
                     ColumnExtractor columnsExtractor, JDBCDriverProvider driverProvider) {
        this.tableExtractor = tableExtractor;
        this.viewExtractor = viewExtractor;
        this.columnsExtractor = columnsExtractor;
        this.driverProvider = driverProvider;
    }

    @Override
    public Database generate(Connection connection) throws Exception {
        log.debug("Starting database generation process.");
        log.debug(checkDrivers());
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);
        java.sql.Connection connect = driver.connect(connection.getUrl(), properties);

        Collection<Table> tables = (Collection<Table>) tableExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, tables);

        Collection<View> views = (Collection<View>) viewExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, views);

        Database database = new Database();
        database.setName(connect.getMetaData().getDatabaseProductName());
        database.setTables(tables);
        database.setViews(views);
        database.setDatabaseType(connection.getDbType());
        connect.close();
        log.debug("Database generation process completed.");
        return database;
    }

    public static List<Path> listJarFiles(String directory) throws IOException {
        if (!Files.exists(Path.of(directory))) {
            log.debug("Directory {} does not exist.", directory);
            return List.of();
        }
        try (Stream<Path> files = Files.walk(Paths.get(directory))) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .collect(Collectors.toList());
        }
    }

    public static String checkDrivers() {
        StringBuilder logMessages = new StringBuilder();
        String rosettaDriversPath = System.getenv("ROSETTA_DRIVERS");
        if (rosettaDriversPath == null) {
            rosettaDriversPath = Paths.get("..", "drivers").toAbsolutePath().toString();
        }

        try {
            List<Path> jarFiles = listJarFiles(rosettaDriversPath);
            if (jarFiles.isEmpty()) {
                String message = "No .jar files found in the directory: " + rosettaDriversPath;
                logMessages.append(message).append("\n");
                log.debug(message);
            } else {
                jarFiles.forEach(path -> {
                    String message = path.toString();
                    logMessages.append(message).append("\n");
                    log.debug(message);
                });
            }
        } catch (IOException e) {
            String errorMessage = "An error occurred while listing jar files: " + e.getMessage();
            logMessages.append(errorMessage).append("\n");
            log.error(errorMessage, e);
        }
        return logMessages.toString();
    }
}