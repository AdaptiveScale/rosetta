package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.PostgresForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.postgres.PostgresDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PostgresDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "postgres");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"ROSETTA\";\n" +
                "CREATE TABLE \"ROSETTA\".\"PLAYER\"(\"Name\" VARCHAR(100), \"Position\" VARCHAR(100), \"Number\" NUMBER NOT NULL );\n" +
                "\r" +
                "CREATE TABLE \"ROSETTA\".\"USER\"(\"USER_ID\" NUMBER NOT NULL , PRIMARY KEY (\"USER_ID\"));\n" +
                "\r", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE \"Position\"(\"ID\" DECIMAL(10) NOT NULL , \"DESCRIPTION\" VARCHAR, \"Name\" VARCHAR, \"TEAMID\" DECIMAL(10), PRIMARY KEY (\"ID\"));\n" +
                "\rCREATE TABLE \"Position2\"(\"ID\" DECIMAL(10) NOT NULL , \"DESCRIPTION\" VARCHAR, \"Name\" VARCHAR, \"TEAMID\" DECIMAL(10), PRIMARY KEY (\"ID\"));\n" +
                "ALTER TABLE \"Position\" ADD CONSTRAINT Position_FK_TEAM FOREIGN KEY (\"TEAMID\") REFERENCES \"TEAM\"(\"ID\") ;\n" +
                "ALTER TABLE \"Position2\" ADD CONSTRAINT Position2_FK_TEAM FOREIGN KEY (\"TEAMID\") REFERENCES \"TEAM\"(\"ID\") ;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addTable2() throws IOException {
        String ddl = generateDDL("add_table2");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"new\";\n" +
                "CREATE TABLE \"new\".\"Position\"(\"ID\" DECIMAL(10) NOT NULL , \"DESCRIPTION\" VARCHAR, \"Name\" VARCHAR, PRIMARY KEY (\"ID\"));\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE IF EXISTS \"TEAMPLAYERS\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE \"Position\" ADD COLUMN \"DESCRIPTION\" varchar(100);\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ADD COLUMN \"POSITION_ID\" numeric;\n" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" ADD CONSTRAINT PLAYER_FK FOREIGN KEY (\"POSITION_ID\") REFERENCES \"FBAL\".\"Position\"(\"ID\") ON DELETE NO ACTION;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ADD COLUMN \"ID\" numeric NOT NULL ;\n" +
                "ALTER TABLE \"PLAYER\" ADD PRIMARY KEY (\"ID\");\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE \"Position\" DROP COLUMN \"DESCRIPTION\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ALTER COLUMN \"name\" SET DATA TYPE INTEGER;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ALTER COLUMN \"ID\" DROP NOT NULL;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ALTER COLUMN \"ID\" SET NOT NULL;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" DROP CONSTRAINT \"PLAYER_FK\";\n" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" DROP COLUMN \"POSITION_ID\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "ALTER TABLE \"PLAYER\" DROP COLUMN \"ID\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK_TEAM\";\n" +
                "DROP TABLE IF EXISTS \"TEAM\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ADD CONSTRAINT PLAYER_FK FOREIGN KEY (\"POSITION_ID\") REFERENCES \"Position\"(\"ID\") ON DELETE NO ACTION;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropPrimaryKey() throws IOException {
        String ddl = generateDDL("drop_primary_key");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "ALTER TABLE \"PLAYER\" DROP CONSTRAINT \"PLAYER_pkey\";\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void addPrimaryKey() throws IOException {
        String ddl = generateDDL("add_primary_key");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" ADD PRIMARY KEY (\"ID\");\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterPrimaryKey() throws IOException {
        String ddl = generateDDL("alter_primary_key");
        Assertions.assertEquals("ALTER TABLE \"PLAYER\" DROP CONSTRAINT \"PLAYER_pkey\";\n" +
                "ALTER TABLE \"PLAYER\" ADD PRIMARY KEY (\"ID\", \"POSITION_ID\");\n" +
                "\r", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT TEAMPLAYERS_CHANGED_FK FOREIGN KEY (\"PLAYERID\") REFERENCES \"PLAYER\"(\"ID\") ON DELETE NO ACTION;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (\"PLAYERID\") REFERENCES \"PLAYER\"(\"ID\") ON DELETE SET NULL;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "ALTER TABLE \"PLAYER\" DROP COLUMN \"ID\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (\"PLAYERID\") REFERENCES \"POSITION\"(\"ID\") ;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"TEAMPLAYERS\" DROP CONSTRAINT \"TEAMPLAYERS_FK\";\n" +
                "DROP TABLE IF EXISTS \"PLAYER\";\n" +
                "ALTER TABLE \"TEAMPLAYERS\" ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (\"PLAYERID\") REFERENCES \"POSITION\"(\"ID\") ;\n", ddl.replaceAll("(?m)^[ \t]*\r?\n", ""));
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        ChangeFinder postgresChangeFinder = new PostgresChangeFinder();
        List<Change<?>> changes = postgresChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new PostgresDDLGenerator(), new PostgresForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
