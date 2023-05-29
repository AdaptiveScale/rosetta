package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.SQLServerForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.sqlserver.SQLServerDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SQLServerDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "sqlserver");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals(("IF NOT EXISTS (SELECT  *    FROM sys.schemas    WHERE name = N'test')EXEC('CREATE SCHEMA test');\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Categories\"(\"CategoryID\" int identity NOT NULL , \"CategoryName\" nvarchar NOT NULL , \"Description\" ntext NOT NULL , \"Picture\" image NOT NULL , PRIMARY KEY (\"CategoryID\"));CREATE TABLE \"test\".\"CustomerCustomerDemo\"(\"CustomerID\" nchar NOT NULL , \"CustomerTypeID\" nchar NOT NULL , PRIMARY KEY (\"CustomerID\", \"CustomerTypeID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"CustomerDemographics\"(\"CustomerTypeID\" nchar NOT NULL , \"CustomerDesc\" ntext NOT NULL , PRIMARY KEY (\"CustomerTypeID\"));CREATE TABLE \"test\".\"Customers\"(\"CustomerID\" nchar NOT NULL , \"CompanyName\" nvarchar NOT NULL , \"ContactName\" nvarchar NOT NULL , \"ContactTitle\" nvarchar NOT NULL , \"Address\" nvarchar NOT NULL , \"City\" nvarchar NOT NULL , \"Region\" nvarchar NOT NULL , \"PostalCode\" nvarchar NOT NULL , \"Country\" nvarchar NOT NULL , \"Phone\" nvarchar NOT NULL , \"Fax\" nvarchar NOT NULL , PRIMARY KEY (\"CustomerID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Employees\"(\"EmployeeID\" int identity NOT NULL , \"LastName\" nvarchar NOT NULL , \"FirstName\" nvarchar NOT NULL , \"Title\" nvarchar NOT NULL , \"TitleOfCourtesy\" nvarchar NOT NULL , \"BirthDate\" datetime NOT NULL , \"HireDate\" datetime NOT NULL , \"Address\" nvarchar NOT NULL , \"City\" nvarchar NOT NULL , \"Region\" nvarchar NOT NULL , \"PostalCode\" nvarchar NOT NULL , \"Country\" nvarchar NOT NULL , \"HomePhone\" nvarchar NOT NULL , \"Extension\" nvarchar NOT NULL , \"Photo\" image NOT NULL , \"Notes\" ntext NOT NULL , \"ReportsTo\" int NOT NULL , \"PhotoPath\" nvarchar NOT NULL , PRIMARY KEY (\"EmployeeID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"EmployeeTerritories\"(\"EmployeeID\" int NOT NULL , \"TerritoryID\" nvarchar NOT NULL , PRIMARY KEY (\"EmployeeID\", \"TerritoryID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"OrderDetails\"(\"OrderDetailsID\" int identity NOT NULL , \"OrderID\" int NOT NULL , \"ProductID\" int NOT NULL , \"UnitPrice\" money NOT NULL , \"Quantity\" smallint NOT NULL , \"Discount\" real NOT NULL , PRIMARY KEY (\"OrderDetailsID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Orders\"(\"OrderID\" int identity NOT NULL , \"CustomerID\" nchar NOT NULL , \"EmployeeID\" int NOT NULL , \"OrderDate\" datetime NOT NULL , \"RequiredDate\" datetime NOT NULL , \"ShippedDate\" datetime NOT NULL , \"ShipVia\" int NOT NULL , \"Freight\" money NOT NULL , \"ShipName\" nvarchar NOT NULL , \"ShipAddress\" nvarchar NOT NULL , \"ShipCity\" nvarchar NOT NULL , \"ShipRegion\" nvarchar NOT NULL , \"ShipPostalCode\" nvarchar NOT NULL , \"ShipCountry\" nvarchar NOT NULL , PRIMARY KEY (\"OrderID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Products\"(\"ProductID\" int identity NOT NULL , \"ProductName\" nvarchar NOT NULL , \"SupplierID\" int NOT NULL , \"CategoryID\" int NOT NULL , \"QuantityPerUnit\" nvarchar NOT NULL , \"UnitPrice\" money NOT NULL , \"UnitsInStock\" smallint NOT NULL , \"UnitsOnOrder\" smallint NOT NULL , \"ReorderLevel\" smallint NOT NULL , \"Discontinued\" bit NOT NULL , PRIMARY KEY (\"ProductID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Region\"(\"RegionID\" int NOT NULL , \"RegionDescription\" nchar NOT NULL , PRIMARY KEY (\"RegionID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Shippers\"(\"ShipperID\" int identity NOT NULL , \"CompanyName\" nvarchar NOT NULL , \"Phone\" nvarchar NOT NULL , PRIMARY KEY (\"ShipperID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Suppliers\"(\"SupplierID\" int identity NOT NULL , \"CompanyName\" nvarchar NOT NULL , \"ContactName\" nvarchar NOT NULL , \"ContactTitle\" nvarchar NOT NULL , \"Address\" nvarchar NOT NULL , \"City\" nvarchar NOT NULL , \"Region\" nvarchar NOT NULL , \"PostalCode\" nvarchar NOT NULL , \"Country\" nvarchar NOT NULL , \"Phone\" nvarchar NOT NULL , \"Fax\" nvarchar NOT NULL , \"HomePage\" ntext NOT NULL , PRIMARY KEY (\"SupplierID\"));\n" +
                "\n" +
                "CREATE TABLE \"test\".\"Territories\"(\"TerritoryID\" nvarchar NOT NULL , \"TerritoryDescription\" nchar NOT NULL , \"RegionID\" int NOT NULL , PRIMARY KEY (\"TerritoryID\"));\n" +
                "\n" +
                "ALTER TABLE \"test\".\"CustomerCustomerDemo\" ADD CONSTRAINT \"FK_CustomerCustomerDemo_Customers\" FOREIGN KEY (\"CustomerID\") REFERENCES \"test\".\"Customers\"(\"CustomerID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"CustomerCustomerDemo\" ADD CONSTRAINT \"FK_CustomerCustomerDemo\" FOREIGN KEY (\"CustomerTypeID\") REFERENCES \"test\".\"CustomerDemographics\"(\"CustomerTypeID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Employees\" ADD CONSTRAINT \"FK_Employees_Employees\" FOREIGN KEY (\"ReportsTo\") REFERENCES \"test\".\"Employees\"(\"EmployeeID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"EmployeeTerritories\" ADD CONSTRAINT \"FK_EmployeeTerritories_Employees\" FOREIGN KEY (\"EmployeeID\") REFERENCES \"test\".\"Employees\"(\"EmployeeID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"EmployeeTerritories\" ADD CONSTRAINT \"FK_EmployeeTerritories_Territories\" FOREIGN KEY (\"TerritoryID\") REFERENCES \"test\".\"Territories\"(\"TerritoryID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"OrderDetails\" ADD CONSTRAINT \"FK_Order_Details_Orders\" FOREIGN KEY (\"OrderID\") REFERENCES \"test\".\"Orders\"(\"OrderID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"OrderDetails\" ADD CONSTRAINT \"FK_Order_Details_Products\" FOREIGN KEY (\"ProductID\") REFERENCES \"test\".\"Products\"(\"ProductID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Orders\" ADD CONSTRAINT \"FK_Orders_Customers\" FOREIGN KEY (\"CustomerID\") REFERENCES \"test\".\"Customers\"(\"CustomerID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Orders\" ADD CONSTRAINT \"FK_Orders_Employees\" FOREIGN KEY (\"EmployeeID\") REFERENCES \"test\".\"Employees\"(\"EmployeeID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Orders\" ADD CONSTRAINT \"FK_Orders_Shippers\" FOREIGN KEY (\"ShipVia\") REFERENCES \"test\".\"Shippers\"(\"ShipperID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Products\" ADD CONSTRAINT \"FK_Products_Suppliers\" FOREIGN KEY (\"SupplierID\") REFERENCES \"test\".\"Suppliers\"(\"SupplierID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Products\" ADD CONSTRAINT \"FK_Products_Categories\" FOREIGN KEY (\"CategoryID\") REFERENCES \"test\".\"Categories\"(\"CategoryID\") ;\n" +
                "\n" +
                "ALTER TABLE \"test\".\"Territories\" ADD CONSTRAINT \"FK_Territories_Region\" FOREIGN KEY (\"RegionID\") REFERENCES \"test\".\"Region\"(\"RegionID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals(("CREATE TABLE \"test\".\"Territories\"(\"TerritoryID\" nvarchar NOT NULL , \"TerritoryDescription\" nchar NOT NULL , \"RegionID\" int NOT NULL , PRIMARY KEY (\"TerritoryID\"));ALTER TABLE \"test\".\"Territories\" ADD CONSTRAINT \"FK_Territories_Region\" FOREIGN KEY (\"RegionID\") REFERENCES \"test\".\"Region\"(\"RegionID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addTable2() throws IOException {
        String ddl = generateDDL("add_table2");
        Assertions.assertEquals(("IF NOT EXISTS (" +
                "SELECT  *    FROM sys.schemas    WHERE name = N'new')" +
                "EXEC('CREATE SCHEMA new');" +
                "CREATE TABLE \"new\".\"Territories\"(\"TerritoryID\" nvarchar NOT NULL , \"TerritoryDescription\" nchar NOT NULL , \"RegionID\" int NOT NULL , PRIMARY KEY (\"TerritoryID\"));ALTER TABLE \"test\".\"Territories\" ADD CONSTRAINT \"FK_Territories_Region\" FOREIGN KEY (\"RegionID\") REFERENCES \"test\".\"Region\"(\"RegionID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTable() throws IOException {
        String ddl = generateDDL("drop_table");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Employees\" DROP CONSTRAINT \"FK_Employees_Employees\";\n" +
                "ALTER TABLE \"test\".\"EmployeeTerritories\" DROP CONSTRAINT \"FK_EmployeeTerritories_Employees\";\n" +
                "ALTER TABLE \"test\".\"Orders\" DROP CONSTRAINT \"FK_Orders_Employees\";\n" +
                "DROP TABLE IF EXISTS \"test\".\"Employees\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE \"test\".\"Categories\" ADD \"Description_NEW\" ntext NOT NULL ;", ddl);
    }

    @Test
    public void addColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("add_column_with_foreign_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Employees\" ADD \"ReportsTo\" int NOT NULL ;" +
                "ALTER TABLE \"test\".\"Employees\" ADD CONSTRAINT \"FK_Employees_Employees\" FOREIGN KEY (\"ReportsTo\") REFERENCES \"test\".\"Employees\"(\"EmployeeID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addColumnAsPrimaryKey() throws IOException {
        String ddl = generateDDL("add_column_as_primary_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Categories\" ADD \"CategoryID2\" int identity NOT NULL ;USE SCHEMA \"test\";" +
                "ALTER TABLE \"Categories\" DROP PRIMARY KEY;USE SCHEMA \"test\";" +
                "ALTER TABLE \"Categories\" ADD PRIMARY KEY (\"CategoryID\", \"CategoryID2\");").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE \"test\".\"Categories\" DROP COLUMN \"Description\";", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE \"test\".\"OrderDetails\" ALTER COLUMN \"Quantity\" SET DATA TYPE int;", ddl);
    }

    @Test
    public void alterColumnToNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_nullable");
        Assertions.assertEquals("ALTER TABLE \"test\".\"Categories\" ALTER COLUMN \"Picture\" DROP NOT NULL;", ddl);
    }

    @Test
    public void alterColumnToNotNullable() throws IOException {
        String ddl = generateDDL("alter_column_to_not_nullable");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Categories\" ALTER COLUMN \"Picture\" SET NOT NULL;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropColumnWithForeignKey() throws IOException {
        String ddl = generateDDL("drop_column_with_foreign_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"CustomerCustomerDemo\" DROP CONSTRAINT \"FK_CustomerCustomerDemo_Customers\";\n" +
                "ALTER TABLE \"test\".\"CustomerCustomerDemo\" DROP COLUMN \"CustomerID\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropColumnWithPrimaryKeyReferenced() throws IOException {
        String ddl = generateDDL("drop_column_with_primary_key_referenced");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Employees\" DROP CONSTRAINT \"FK_Employees_Employees\";" +
                "ALTER TABLE \"test\".\"EmployeeTerritories\" DROP CONSTRAINT \"FK_EmployeeTerritories_Employees\";" +
                "ALTER TABLE \"test\".\"Orders\" DROP CONSTRAINT \"FK_Orders_Employees\";" +
                "DROP TABLE IF EXISTS \"test\".\"Employees\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTableWhereColumnIsReferenced() throws IOException {
        String ddl = generateDDL("drop_table_where_column_is_referenced");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Employees\" DROP CONSTRAINT \"FK_Employees_Employees\";ALTER TABLE \"test\".\"EmployeeTerritories\" DROP CONSTRAINT \"FK_EmployeeTerritories_Employees\";ALTER TABLE \"test\".\"Orders\" DROP CONSTRAINT \"FK_Orders_Employees\";DROP TABLE IF EXISTS \"test\".\"Employees\";").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addForeignKey() throws IOException {
        String ddl = generateDDL("add_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"test\".\"Employees\" ADD CONSTRAINT \"FK_Employees_Employees\" FOREIGN KEY (\"ReportsTo\") REFERENCES \"test\".\"Employees\"(\"EmployeeID\") ;\n", ddl);
    }

    @Test
    public void dropForeignKey() throws IOException {
        String ddl = generateDDL("drop_foreign_key");
        Assertions.assertEquals("ALTER TABLE \"test\".\"CustomerCustomerDemo\" DROP CONSTRAINT \"FK_CustomerCustomerDemo_Customers\";", ddl);
    }

    @Test
    public void dropPrimaryKey() throws IOException {
        String ddl = generateDDL("drop_primary_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Products\" DROP CONSTRAINT \"FK_Products_Categories\";" +
                "USE SCHEMA \"test\";ALTER TABLE \"Categories\" DROP PRIMARY KEY;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void addPrimaryKey() throws IOException {
        String ddl = generateDDL("add_primary_key");
        Assertions.assertEquals("USE SCHEMA \"test\";\n" +
                "ALTER TABLE \"Categories\" DROP PRIMARY KEY;USE SCHEMA \"test\";\n" +
                "ALTER TABLE \"Categories\" ADD PRIMARY KEY (\"Description\", \"CategoryID\");", ddl);
    }

    @Test
    public void alterPrimaryKey() throws IOException {
        String ddl = generateDDL("alter_primary_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"CustomerCustomerDemo\" DROP CONSTRAINT \"FK_CustomerCustomerDemo_Customers\";" +
                "USE SCHEMA \"test\";" +
                "ALTER TABLE \"Categories\" DROP PRIMARY KEY;" +
                "USE SCHEMA \"test\";ALTER TABLE \"Categories\" ADD PRIMARY KEY (\"Description\", \"CategoryID\");" +
                "ALTER TABLE \"test\".\"CustomerCustomerDemo\" ADD CONSTRAINT \"FK_CustomerCustomerDemo_Customers_NEW\" FOREIGN KEY (\"CustomerID\") REFERENCES \"test\".\"Customers\"(\"CustomerID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void alterForeignKeyName() throws IOException {
        String ddl = generateDDL("alter_foreign_key_name");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"CustomerCustomerDemo\" DROP CONSTRAINT \"FK_CustomerCustomerDemo_Customers\";" +
                "ALTER TABLE \"test\".\"CustomerCustomerDemo\" ADD CONSTRAINT \"FK_CustomerCustomerDemo_Customers_NEW\" FOREIGN KEY (\"CustomerID\") REFERENCES \"test\".\"Customers\"(\"CustomerID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void alterForeignKey() throws IOException {
        String ddl = generateDDL("alter_foreign_key");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"Territories\" DROP CONSTRAINT \"FK_Territories_Region\";ALTER TABLE \"test\".\"Territories\" ADD CONSTRAINT \"FK_Territories_Region\" FOREIGN KEY (\"RegionID\") REFERENCES \"test\".\"Region\"(\"RegionID\") ;" +
                "\n").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_pk_column_and_alter_fk");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"EmployeeTerritories\" DROP CONSTRAINT \"FK_EmployeeTerritories_Employees\";" +
                "ALTER TABLE \"test\".\"Orders\" DROP CONSTRAINT \"FK_Orders_Employees\";" +
                "ALTER TABLE \"test\".\"Orders\" DROP COLUMN \"EmployeeID\";" +
                "ALTER TABLE \"test\".\"Orders\" ADD CONSTRAINT \"FK_Orders_Shippers\" FOREIGN KEY (\"ShipVia\") REFERENCES \"test\".\"Shippers\"(\"ShipperID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    @Test
    public void dropTableWithPrimaryKeyColumnAndAlterForeignKey() throws IOException {
        String ddl = generateDDL("drop_table_with_pk_column_and_alter_fk");
        Assertions.assertEquals(("ALTER TABLE \"test\".\"EmployeeTerritories\" DROP CONSTRAINT \"FK_EmployeeTerritories_Employees\";" +
                "ALTER TABLE \"test\".\"Orders\" DROP CONSTRAINT \"FK_Orders_Employees\";" +
                "ALTER TABLE \"test\".\"Employees\" DROP CONSTRAINT \"FK_Employees_Employees\";" +
                "DROP TABLE IF EXISTS \"test\".\"Employees\";" +
                "ALTER TABLE \"test\".\"Orders\" DROP COLUMN \"EmployeeID\";" +
                "ALTER TABLE \"test\".\"Orders\" ADD CONSTRAINT \"FK_Orders_Shippers\" FOREIGN KEY (\"ShipVia\") REFERENCES \"test\".\"Shippers\"(\"ShipperID\") ;").replaceAll("(\\r|\\n|\\t)", ""), ddl.replaceAll("(\\r|\\n|\\t)", ""));
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        ChangeFinder sqlServerChangeFinder = new SQLServerChangeFinder();
        List<Change<?>> changes = sqlServerChangeFinder.findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new SQLServerDDLGenerator(), new SQLServerForeignKeyChangeComparator());
        return handler.createDDLForChanges(changes);
    }
}
