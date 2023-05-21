/*
 *  Copyright 2022 AdaptiveScale
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.KineticaForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.kinetica.KineticaDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class KineticaDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "kinetica");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"ROSETTA\";\n" +
                "CREATE TABLE \"ROSETTA\".\"PLAYER\"(\"Name\" VARCHAR(100), \"Position\" VARCHAR(100), \"Number\" NUMBER NOT NULL );\r" +
                "\r" +
                "CREATE SCHEMA IF NOT EXISTS \"ROSETTA\";\n" +
                "CREATE TABLE \"ROSETTA\".\"USER\"(\"USER_ID\" NUMBER NOT NULL , PRIMARY KEY (\"USER_ID\"));", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS \"ROSETTA\";\n" +
                "CREATE TABLE \"ROSETTA\".\"Position\"(\"ID\" DECIMAL(10) NOT NULL , \"DESCRIPTION\" VARCHAR," +
                " \"Name\" VARCHAR, PRIMARY KEY (\"ID\"));", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE IF EXISTS \"ROSETTA\".\"TEAMPLAYERS\";", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"Position\" ADD \"DESCRIPTION\" varchar(100);", ddl);
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" ADD \"POSITION_ID\" numeric;\r" +
                "ALTER TABLE \"ROSETTA\".\"PLAYER\" ADD FOREIGN KEY (\"POSITION_ID\") REFERENCES \"ROSETTA\".\"Position\"(\"ID\") AS PLAYER_FK;", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"Position\" DROP COLUMN \"DESCRIPTION\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" MODIFY \"name\" INTEGER;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" MODIFY \"ID\" numeric;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" MODIFY \"ID\" numeric NOT NULL ;", ddl);
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"FBAL\".\"PLAYER\" DROP FOREIGN KEY \"PLAYER_FK\";\r" +
                "ALTER TABLE \"FBAL\".\"PLAYER\" DROP COLUMN \"POSITION_ID\";", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";\r" +
                "ALTER TABLE \"ROSETTA\".\"PLAYER\" DROP COLUMN \"ID\";", ddl);
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK_TEAM\";\r" +
                "DROP TABLE IF EXISTS \"ROSETTA\".\"TEAM\";", ddl);
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"PLAYER\" ADD FOREIGN KEY (\"POSITION_ID\") " +
                "REFERENCES \"ROSETTA\".\"Position\"(\"ID\") AS PLAYER_FK;", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";", ddl);
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";\r" +
                "ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" ADD FOREIGN KEY (\"PLAYERID\") REFERENCES " +
                "\"ROSETTA\".\"PLAYER\"(\"ID\") AS TEAMPLAYERS_CHANGED_FK;", ddl);
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";\r" +
                "ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" ADD FOREIGN KEY (\"PLAYERID\") REFERENCES \"ROSETTA\".\"PLAYER\"(\"ID\") AS TEAMPLAYERS_FK;", ddl);
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";\r" +
                "ALTER TABLE \"ROSETTA\".\"PLAYER\" DROP COLUMN \"ID\";\r" +
                "ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" ADD FOREIGN KEY (\"PLAYERID\") REFERENCES \"ROSETTA\".\"POSITION\"(\"ID\") AS TEAMPLAYERS_FK;", ddl);
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" DROP FOREIGN KEY \"TEAMPLAYERS_FK\";\r" +
                "DROP TABLE IF EXISTS \"ROSETTA\".\"PLAYER\";\r" +
                "ALTER TABLE \"ROSETTA\".\"TEAMPLAYERS\" ADD FOREIGN KEY (\"PLAYERID\") REFERENCES \"ROSETTA\".\"POSITION\"(\"ID\") AS TEAMPLAYERS_FK;", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        ChangeFinder kineticaChangeFinder = new KineticaChangeFinder();
        List<Change<?>> changes = kineticaChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new KineticaDDLGenerator(), new KineticaForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
