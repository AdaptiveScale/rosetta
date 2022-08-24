package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.MysqlForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.mysql.MySqlDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MySqlDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "mysql");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE SCHEMA IF NOT EXISTS `ROSETTA`;\r" +
                "CREATE TABLE `ROSETTA`.`PLAYER`(`Name` VARCHAR(100) NULL, `Position` VARCHAR(100) NULL, `Number` NUMBER NOT NULL);\r" +
                "\r" +
                "CREATE TABLE `ROSETTA`.`USER`(`USER_ID` NUMBER NOT NULL, PRIMARY KEY (`USER_ID`));", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE `Position`(`ID` DECIMAL NOT NULL, `DESCRIPTION` VARCHAR(0) NULL," +
                " `Name` VARCHAR(0) NULL, PRIMARY KEY (`ID`));", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE `TEAMPLAYERS`;", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE `Position` ADD COLUMN `DESCRIPTION` varchar(0) NULL;", ddl);
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE `PLAYER` ADD COLUMN `POSITION_ID` numeric NULL;\r" +
                "ALTER TABLE `FBAL`.`PLAYER` ADD CONSTRAINT PLAYER_FK FOREIGN KEY (`POSITION_ID`) REFERENCES  `FBAL`.`Position`(`ID`) ON DELETE NO ACTION ;\r", ddl);
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals("ALTER TABLE `PLAYER` ADD COLUMN `ID` numeric NOT NULL;\r" +
                "ALTER TABLE `PLAYER` ADD PRIMARY KEY (`ID`);", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE `Position` DROP COLUMN `DESCRIPTION`;", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE `PLAYER` MODIFY `name` INTEGER NULL;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE `PLAYER` MODIFY `ID` numeric NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("ALTER TABLE `PLAYER` MODIFY `ID` numeric NOT NULL;", ddl);
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE `FBAL`.`PLAYER` DROP FOREIGN KEY `PLAYER_FK`;\r" +
                "ALTER TABLE `FBAL`.`PLAYER` DROP COLUMN `POSITION_ID`;", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "ALTER TABLE `PLAYER` DROP COLUMN `ID`;", ddl);
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK_TEAM`;\r" +
                "DROP TABLE `TEAM`;", ddl);
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE `PLAYER` ADD CONSTRAINT PLAYER_FK FOREIGN KEY (`POSITION_ID`) " +
                "REFERENCES  `Position`(`ID`) ON DELETE NO ACTION ;\r", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;", ddl);
    }

    @Test
    public void dropPrimaryKey() throws IOException {
        String ddl = generateDDL("drop_primary_key");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "ALTER TABLE `PLAYER` DROP PRIMARY KEY;", ddl);
    }

    @Test
    public void addPrimaryKey() throws IOException {
        String ddl = generateDDL("add_primary_key");
        Assertions.assertEquals("ALTER TABLE `PLAYER` ADD PRIMARY KEY (`ID`);", ddl);
    }

    @Test
    public void alterPrimaryKey() throws IOException {
        String ddl = generateDDL("alter_primary_key");
        Assertions.assertEquals("\r" +
                "ALTER TABLE `PLAYER` DROP PRIMARY KEY, ADD PRIMARY KEY (`ID`, `POSITION_ID`);\r", ddl);
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "ALTER TABLE `TEAMPLAYERS` ADD CONSTRAINT TEAMPLAYERS_CHANGED_FK FOREIGN KEY (`PLAYERID`) REFERENCES  " +
                "`PLAYER`(`ID`) ON DELETE NO ACTION ;\r", ddl);
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "ALTER TABLE `TEAMPLAYERS` ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (`PLAYERID`) REFERENCES  `PLAYER`(`ID`) ON DELETE SET NULL ;\r", ddl);
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "ALTER TABLE `PLAYER` DROP COLUMN `ID`;\r" +
                "ALTER TABLE `TEAMPLAYERS` ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (`PLAYERID`) REFERENCES  `POSITION`(`ID`);\r", ddl);
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE `TEAMPLAYERS` DROP FOREIGN KEY `TEAMPLAYERS_FK`;\r" +
                "DROP TABLE `PLAYER`;\r" +
                "ALTER TABLE `TEAMPLAYERS` ADD CONSTRAINT TEAMPLAYERS_FK FOREIGN KEY (`PLAYERID`) REFERENCES  `POSITION`(`ID`);\r", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        MySQLChangeFinder mySQLChangeFinder = new MySQLChangeFinder();
        List<Change<?>> changes = mySQLChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new MySqlDDLGenerator(), new MysqlForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
