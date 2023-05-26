package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.ChangeHandlerImplementation;
import com.adaptivescale.rosetta.ddl.change.DefaultChangeFinder;
import com.adaptivescale.rosetta.ddl.change.comparator.SnowflakeChangesComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.snowflake.SnowflakeDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SnowflakeDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "snowflake");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"ROSETTA\";\r" +
                "USE SCHEMA \"ROSETTA\";\n" +
                "CREATE TABLE \"PLAYER\"(\"Name\" VARCHAR, \"Position\" VARCHAR, \"Number\" NUMBER not null);\r" +
                "USE SCHEMA \"ROSETTA\";\n" +
                "CREATE TABLE \"USER\"(\"USER_ID\" NUMBER not null, PRIMARY KEY (\"USER_ID\"));", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "CREATE TABLE \"PLAYER\"(\"Name\" VARCHAR, \"Position\" VARCHAR, \"Number\" NUMBER not null);", ddl);
    }

    @Test
    public void addTable2() throws IOException {
        String ddl = generateDDL("add_table2");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"NEW\";\r" +
                "USE SCHEMA \"NEW\";\n" +
                "CREATE TABLE \"PLAYER\"(\"Name\" VARCHAR, \"Position\" VARCHAR, \"Number\" NUMBER not null);", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "DROP TABLE IF EXISTS \"PLAYER\";", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "ALTER TABLE \"PLAYER\" ADD \"Position\" VARCHAR not null;", ddl);
    }


    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD \"POSITION_ID\" NUMBER;\r" +
                "USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD CONSTRAINT \"PLAYER_FK\" FOREIGN KEY (\"POSITION_ID\") REFERENCES \"Position\"(\"ID\") ON DELETE NO ACTION;\n", ddl);
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD \"ID\" NUMBER not null;\r" +
                "USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD PRIMARY KEY (\"ID\");", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "ALTER TABLE \"PLAYER\" DROP COLUMN \"Position\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "ALTER TABLE \"PLAYER\" ALTER \"Number\" SET DATA TYPE INTEGER;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "ALTER TABLE \"PLAYER\" ALTER \"Number\" DROP NOT NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("USE SCHEMA \"ROSETTA\";\n" +
                "ALTER TABLE \"PLAYER\" ALTER \"Position\" SET NOT NULL;", ddl);
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" DROP COLUMN \"POSITION_ID\";", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"Position\" DROP COLUMN \"ID\";", ddl);
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "DROP TABLE IF EXISTS \"Position\";", ddl);
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD \"name\" VARCHAR;\r" +
                "USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"PLAYER\" ADD CONSTRAINT \"PLAYER_FK\" FOREIGN KEY (\"POSITION_ID\") REFERENCES \"Position\"(\"ID\") ON DELETE NO ACTION;\n", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM\";", ddl);
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM_CHANGED\";\r" +
                "USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT \"TEAMPLAYERS_FK_TEAM\" FOREIGN KEY (\"TEAMID\") REFERENCES \"TEAM\"(\"ID\") ON DELETE NO ACTION;\n", ddl);
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM\";\r" +
                "USE SCHEMA \"FBAL\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT \"TEAMPLAYERS_FK_TEAM\" FOREIGN KEY (\"TEAMID\") REFERENCES \"TEAM\"(\"ID\") ON DELETE NO ACTION;\n", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        SnowflakeDDLGenerator snowflakeDDLGenerator = new SnowflakeDDLGenerator();
        DefaultChangeFinder defaultChangeFinder = new DefaultChangeFinder();
        List<Change<?>> changes = defaultChangeFinder.findChanges(expected, actual);
        return new ChangeHandlerImplementation(snowflakeDDLGenerator, new SnowflakeChangesComparator()).createDDLForChanges(changes);
    }
}
