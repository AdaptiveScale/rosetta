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
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS ROSETTA;\r" +
                "CREATE TABLE \"ROSETTA\".\"PLAYER\"(Name VARCHAR, Position VARCHAR, Number NUMBER not null);\r" +
                "\r" +
                "CREATE TABLE \"ROSETTA\".\"USER\"(USER_ID NUMBER not null, PRIMARY KEY (USER_ID));", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE \"ROSETTA\".\"PLAYER\"(Name VARCHAR, Position VARCHAR, Number NUMBER not null);", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE \"FBAL\".\"PLAYER\";", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" add Position VARCHAR not null;", ddl);
    }


    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" add POSITION_ID NUMBER;\r" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" ADD CONSTRAINT \"PLAYER_FK\" FOREIGN KEY (\"POSITION_ID\") REFERENCES \"FBAL\".\"Position\"(\"ID\") ON DELETE NO ACTION;\r", ddl);
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" add ID NUMBER not null;\r" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" ADD PRIMARY KEY (ID);", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" DROP \"Position\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" ALTER  \"Number\" SET DATA TYPE INTEGER;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" ALTER  \"Number\" DROP NOT NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" ALTER  \"Position\" SET NOT NULL;", ddl);
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" DROP \"POSITION_ID\";", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"Position\" DROP \"ID\";", ddl);
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("DROP TABLE \"FBAL\".\"Position\";", ddl);
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" add name VARCHAR;\r" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" ADD CONSTRAINT \"PLAYER_FK\" FOREIGN KEY (\"POSITION_ID\") REFERENCES \"FBAL\".\"Position\"(\"ID\") ON DELETE NO ACTION;\r", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM\";", ddl);
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM_CHANGED\";\r" +
                "ALTER TABLE \"FBAL\".\"TEAMPLAYERS\" ADD CONSTRAINT \"TEAMPLAYERS_FK_TEAM\" FOREIGN KEY (\"TEAMID\") REFERENCES \"FBAL\".\"TEAM\"(\"ID\") ON DELETE NO ACTION;\r", ddl);
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM\";\r" +
                "ALTER TABLE \"FBAL\".\"TEAMPLAYERS\" ADD CONSTRAINT \"TEAMPLAYERS_FK_TEAM\" FOREIGN KEY (\"TEAMID\") REFERENCES \"FBAL\".\"TEAM\"(\"ID\") ON DELETE NO ACTION;\r", ddl);
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
