package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.ChangeHandler;
import com.adaptivescale.rosetta.ddl.change.ChangeHandlerImplementation;
import com.adaptivescale.rosetta.ddl.change.DefaultChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.bigquery.BigQueryDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BigQueryDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "bigquery_ddl");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS halis;\r" +
                "CREATE TABLE `halis`.`tableA`(`columnA` STRING, `columnB` INT64);\r" +
                "CREATE TABLE `halis`.`tableB`(`columnA` STRING, `columnB` INT64);", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE `halis`.`tableB`(`columnA` STRING, `columnB` INT64);", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE halis.tableB;", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE halis.tableA ADD COLUMN `columnB` INT64;", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE halis.tableA DROP COLUMN columnB;", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE halis.tableA ALTER COLUMN columnB SET DATA TYPE STRING;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE halis.tableA ALTER COLUMN columnB DROP NOT NULL;", ddl);
    }

    @Test
    public void addView() throws IOException {
        String ddl = generateDDL("add_view");
        Assertions.assertEquals("CREATE VIEW `halis.viewB`\r\n" +
                "select * from tableB limit 1\r\n" +
                ";", ddl);
    }

    @Test
    public void dropView() throws IOException {
        String ddl = generateDDL("drop_view");
        Assertions.assertEquals("DROP VIEW IF EXISTS `halis.viewB`;", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        List<Change<?>> changes = new DefaultChangeFinder().findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new BigQueryDDLGenerator(), null);
        return handler.createDDLForChanges(changes);
    }
}
