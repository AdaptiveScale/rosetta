package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.OracleForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.oracle.OracleDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class OracleDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "oracle");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals(("\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "CREATE TABLE \"CUSTOMERS\"(\"CUSTOMER_ID\" NUMBER(10) NOT NULL , \"CUSTOMER_NAME\" VARCHAR2(100) NOT NULL , \"CUSTOMER_EMAIL\" VARCHAR2(100), \"CUSTOMER_ADDRESS\" VARCHAR2(200), \"CUSTOMER_PHONE\" VARCHAR2(20), PRIMARY KEY (\"CUSTOMER_ID\"));\n" +
                "\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "CREATE TABLE \"ORDERS\"(\"ORDER_ID\" NUMBER(10) NOT NULL , \"ORDER_DATE\" DATE, \"ORDER_TOTAL\" NUMBER(10), \"CUSTOMER_ID\" NUMBER(10), PRIMARY KEY (\"ORDER_ID\"));\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDERS\"\n" +
                "ADD CONSTRAINT \"FK_CUSTOMER_ID\"\n" +
                "FOREIGN KEY (\"CUSTOMER_ID\")\n" +
                "REFERENCES \"CUSTOMERS\"(\"CUSTOMER_ID\");\n" +
                "\n" +
                "\n" +
                "\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "CREATE TABLE \"ORDER_ITEMS\"(\"ITEM_ID\" NUMBER(10) NOT NULL , \"ORDER_ID\" NUMBER(10), \"PRODUCT_NAME\" VARCHAR2(100), \"QUANTITY\" NUMBER(5), \"PRICE_PER_UNIT\" NUMBER(10), PRIMARY KEY (\"ITEM_ID\"));\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_ORDER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"ORDERS\"(\"ORDER_ID\");\n" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "CREATE TABLE \"ORDER_ITEMS_NEW\"(\"ITEM_ID\" NUMBER(10) NOT NULL , \"ORDER_ID\" NUMBER(10), \"PRODUCT_NAME\" VARCHAR2(100), \"QUANTITY\" NUMBER(5), \"PRICE_PER_UNIT\" NUMBER(10), PRIMARY KEY (\"ITEM_ID\"));\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_ORDER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"ORDERS\"(\"ORDER_ID\");").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "DROP TABLE \"ORDER_ITEMS\";", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD \"QUANTITY_NEW\" NUMBER(5);", ddl);
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDERS\"\n" +
                "ADD \"CUSTOMER_ID_NEW\" NUMBER(10);\r" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\r" +
                "ALTER TABLE \"ORDERS\"\n" +
                "ADD CONSTRAINT \"FK_CUSTOMER_ID_NEW\"\n" +
                "FOREIGN KEY (\"CUSTOMER_ID_NEW\")\n" +
                "REFERENCES \"CUSTOMERS\"(\"CUSTOMER_ID\");\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD \"ITEM_ID_2\" NUMBER(10) NOT NULL ;\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP PRIMARY KEY;\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD PRIMARY KEY (\"ITEM_ID\", \"ITEM_ID_2\");\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP COLUMN \"PRICE_PER_UNIT\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "MODIFY \"PRICE_PER_UNIT\" LONG;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "MODIFY \"PRICE_PER_UNIT\" DROP NOT NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "MODIFY \"PRICE_PER_UNIT\" SET NOT NULL;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDERS\"\n" +
                "DROP COLUMN \"ORDER_ID\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "DROP TABLE \"ORDERS\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_ORDER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"ORDERS\"(\"ORDER_ID\");\n" +
                "\n", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";", ddl);
    }

    @Test
    public void dropPrimaryKey() throws IOException {
        String ddl = generateDDL("drop_primary_key");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDERS\"\n" +
                "DROP CONSTRAINT \"FK_CUSTOMER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"CUSTOMERS\"\n" +
                "DROP PRIMARY KEY;\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addPrimaryKey() throws IOException {
        String ddl = generateDDL("add_primary_key");
        Assertions.assertEquals("ALTER SESSION SET CURRENT_SCHEMA = \"WEBSTORE\";\n" +
                "ALTER TABLE \"CUSTOMER\"\n" +
                "ADD PRIMARY KEY (\"C_CUSTOMER_SK\");\n", ddl);
    }

    @Test
    public void alterPrimaryKey() throws IOException {
        String ddl = generateDDL("alter_primary_key");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"CUSTOMERS\"\n" +
                "DROP PRIMARY KEY;\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"CUSTOMERS\"\n" +
                "ADD PRIMARY KEY (\"CUSTOMER_NAME\", \"CUSTOMER_ID\");\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_ORDER_ID_NEW_NAME\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"ORDERS\"(\"ORDER_ID\");\n" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_ORDER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"ORDERS\"(\"ORDER_ID\");\n" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDERS\"\n" +
                "DROP COLUMN \"ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_CUSTOMER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"CUSTOMERS\"(\"CUSTOMER_ID\");\n" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals(("ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "DROP CONSTRAINT \"FK_ORDER_ID\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "DROP TABLE \"ORDERS\";\n" +
                "ALTER SESSION SET CURRENT_SCHEMA = \"ROSETTA\";\n" +
                "ALTER TABLE \"ORDER_ITEMS\"\n" +
                "ADD CONSTRAINT \"FK_CUSTOMER_ID\"\n" +
                "FOREIGN KEY (\"ORDER_ID\")\n" +
                "REFERENCES \"CUSTOMERS\"(\"CUSTOMER_ID\");\n" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        ChangeFinder oracleChangeFinder = new OracleChangeFinder();
        List<Change<?>> changes = oracleChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new OracleDDLGenerator(), new OracleForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
