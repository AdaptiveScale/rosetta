package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.DB2ForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.db2.DB2DDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DB2DDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "db2");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("\r" +
                "CREATE TABLE \"WEBSTORE\".\"CUSTOMER\"(\"C_SALUTATION\" VARCHAR(5), \"C_LAST_NAME\" VARCHAR(20), \"C_FIRST_NAME\" VARCHAR(20), \"C_CUSTOMER_SK\" INTEGER NOT NULL WITH DEFAULT , PRIMARY KEY (\"C_CUSTOMER_SK\"));\r" +
                        "CREATE TABLE \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\" INTEGER NOT NULL WITH DEFAULT , \"INV_QUANTITY_ON_HAND\" INTEGER NOT NULL WITH DEFAULT , PRIMARY KEY (\"INV_ITEM_SK\"));\r" +
                        "CREATE TABLE \"WEBSTORE\".\"TESTJSON\"(\"JSON_FIELD\" BLOB);\r" +
                        "CREATE TABLE \"WEBSTORE\".\"WEBSALES\"(\"WS_ORDER_NUMBER\" INTEGER NOT NULL WITH DEFAULT , \"WS_CUSTOMER_SK\" INTEGER, \"WS_QUANTITY\" INTEGER NOT NULL WITH DEFAULT , \"WS_ITEM_SK\" INTEGER, PRIMARY KEY (\"WS_ORDER_NUMBER\"));\r" +
                        "CREATE TABLE \"WEBSTORE\".\"CUSTOMER_NEW\"(\"C_SALUTATION\" VARCHAR(5), \"C_LAST_NAME\" VARCHAR(20), \"C_FIRST_NAME\" VARCHAR(20), \"C_CUSTOMER_SK\" INTEGER NOT NULL WITH DEFAULT , \"C_AGE_NEW\" INTEGER, PRIMARY KEY (\"C_CUSTOMER_SK\"));\r" +
                        "CREATE TABLE \"WEBSTORE\".\"WEBSALES_NEW\"(\"WS_ORDER_NUMBER\" INTEGER NOT NULL WITH DEFAULT , \"WS_CUSTOMER_SK\" INTEGER, \"WS_ITEM_SK\" INTEGER, PRIMARY KEY (\"WS_ORDER_NUMBER\"));\r" +
                        "ALTER TABLE \"WEBSTORE\".\"WEBSALES\" ADD CONSTRAINT \"CUSTOMER_SK\" FOREIGN KEY (\"WS_CUSTOMER_SK\") REFERENCES \"WEBSTORE\".\"CUSTOMER\"(\"C_CUSTOMER_SK\") ON DELETE NO ACTION NOT ENFORCED;\n" +
                        "ALTER TABLE \"WEBSTORE\".\"WEBSALES\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n" +
                        "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"CUSTOMER_SK\" FOREIGN KEY (\"WS_CUSTOMER_SK\") REFERENCES \"WEBSTORE\".\"CUSTOMER\"(\"C_CUSTOMER_SK\") ON DELETE NO ACTION NOT ENFORCED;\n" +
                        "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE \"WEBSTORE\".\"WEBSALES_NEW\"(\"WS_ORDER_NUMBER\" INTEGER NOT NULL WITH DEFAULT , \"WS_CUSTOMER_SK\" INTEGER, \"WS_ITEM_SK\" INTEGER, PRIMARY KEY (\"WS_ORDER_NUMBER\"));\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"CUSTOMER_SK\" FOREIGN KEY (\"WS_CUSTOMER_SK\") REFERENCES \"WEBSTORE\".\"CUSTOMER\"(\"C_CUSTOMER_SK\") ON DELETE NO ACTION NOT ENFORCED;\n" +
                "\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals("DROP TABLE \"WEBSTORE\".\"WEBSALES_NEW\";", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD \"WS_QUANTITY\" DOUBLE;", ddl);
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD \"WS_ORDER_NUMBER2\" INTEGER NOT NULL WITH DEFAULT ;\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP PRIMARY KEY;\n" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\"\n" +
                "ADD CONSTRAINT PK_WEBSALES_NEW\n" +
                "PRIMARY KEY (\"WS_ORDER_NUMBER\", \"WS_ORDER_NUMBER2\");", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP COLUMN \"WS_QUANTITY\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ALTER COLUMN \"WS_ORDER_NUMBER\" SET DATA TYPE DOUBLE;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ALTER COLUMN \"WS_ORDER_NUMBER\" DROP NOT NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ALTER COLUMN \"WS_ORDER_NUMBER\" SET NOT NULL;", ddl);
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP COLUMN \"WS_ITEM_SK\";", ddl);
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"INVENTORY\" DROP COLUMN \"INV_ITEM_SK\";", ddl);
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "DROP TABLE \"WEBSTORE\".\"INVENTORY\";", ddl);
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";", ddl);
    }

    @Test
    public void dropPrimaryKey() throws IOException {
        String ddl = generateDDL("drop_primary_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES\" DROP CONSTRAINT \"CUSTOMER_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"CUSTOMER_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"CUSTOMER\" DROP PRIMARY KEY;\n", ddl);
    }

    @Test
    public void addPrimaryKey() throws IOException {
        String ddl = generateDDL("add_primary_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"CUSTOMER\"\n" +
                "ADD CONSTRAINT PK_CUSTOMER\n" +
                "PRIMARY KEY (\"C_CUSTOMER_SK\");", ddl);
    }

    @Test
    public void alterPrimaryKey() throws IOException {
        String ddl = generateDDL("alter_primary_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"CUSTOMER\" DROP PRIMARY KEY;\n" +
                "ALTER TABLE \"WEBSTORE\".\"CUSTOMER\"\n" +
                "ADD CONSTRAINT PK_CUSTOMER\n" +
                "PRIMARY KEY (\"C_CUSTOMER_SK\", \"C_CUSTOMER_SK2\");", ddl);
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK_NEW\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"ITEM_SK\" FOREIGN KEY (\"WS_ITEM_SK\") REFERENCES \"WEBSTORE\".\"INVENTORY\"(\"INV_ITEM_SK\") ON DELETE SET NULL NOT ENFORCED;\n", ddl);
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"INVENTORY\" DROP COLUMN \"INV_ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" ADD CONSTRAINT \"CUSTOMER_SK\" FOREIGN KEY (\"WS_CUSTOMER_SK\") REFERENCES \"WEBSTORE\".\"CUSTOMER\"(\"C_CUSTOMER_SK\") ON DELETE NO ACTION NOT ENFORCED;\n", ddl);
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals("ALTER TABLE \"WEBSTORE\".\"WEBSALES\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "ALTER TABLE \"WEBSTORE\".\"WEBSALES_NEW\" DROP CONSTRAINT \"ITEM_SK\";\r" +
                "DROP TABLE \"WEBSTORE\".\"INVENTORY\";", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        ChangeFinder db2ChangeFinder = new DB2ChangeFinder();
        List<Change<?>> changes = db2ChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new DB2DDLGenerator(), new DB2ForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
