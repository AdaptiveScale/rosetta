package integration;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.test.Tests;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.ddl.change.SQLServerChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.DDLExecutor;
import com.adaptivescale.rosetta.test.assertion.AssertionSqlGenerator;
import com.adaptivescale.rosetta.test.assertion.DefaultAssertTestEngine;
import com.adaptivescale.rosetta.test.assertion.DefaultSqlExecution;
import com.adaptivescale.rosetta.test.assertion.generator.AssertionSqlGeneratorFactory;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqlserverIntegrationTest {

    private static String IMAGE = "mcr.microsoft.com/mssql/server:2022-latest";
    private static String USERNAME = "SA";
    private static String PASSWORD = "123abcD!";
    private static String DATABASE = "Northwind";

    private static String SCHEMA = "test";
    private static String DB_TYPE = "sqlserver";

    private static int PORT = 1433;

    private static String CREATE_DDL1 = "CREATE TABLE Categories (" +
            " CategoryID int IDENTITY(1,1) NOT NULL, " +
            " CategoryName nvarchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL, " +
            " Description ntext COLLATE SQL_Latin1_General_CP1_CI_AS NULL, " +
            " Picture image NULL," +
            " CONSTRAINT PK_Categories PRIMARY KEY (CategoryID) " +
            ");";
    
    private static String CREATE_DDL2 = "CREATE TABLE TestTable ( " +
            "    TestBit bit, " +
            "    TestTinyInt tinyint, " +
            "    TestSmallInt smallint, " +
            "    TestInt int, " +
            "    TestBigInt bigint, " +
            "    TestDecimal decimal(18, 4), " +
            "    TestNumeric numeric(18, 4), " +
            "    TestSmallMoney smallmoney, " +
            "    TestMoney money, " +
            "    TestFloat float, " +
            "    TestReal real, " +
            "    TestDate date, " +
            "    TestTime time, " +
            "    TestDateTime datetime, " +
            "    TestDateTime2 datetime2, " +
            "    TestDateTimeOffset datetimeoffset, " +
            "    TestChar char, " +
            "    TestVarChar varchar, " +
            "    TestText text, " +
            "    TestNChar nchar, " +
            "    TestNVarChar nvarchar, " +
            "    TestNText ntext, " +
            "    TestBinary binary, " +
            "    TestVarBinary varbinary, " +
            "    TestImage image, " +
            "    TestUniqueIdentifier uniqueidentifier, " +
            "    TestXml xml, " +
//            "    TestCursor cursor, " +
            "    TestHierarchyId hierarchyid, " +
            "    TestSqlVariant sql_variant, " +
//            "    TestTableType table, " +
            "    TestGeometry geometry, " +
            "    TestGeography geography, " +
            "    TestRowVersion rowversion, " +
//            "    TestTimeStamp timestamp, " +
            "    TestIntIdentity int identity " +
            ");";

    public static String CREATE_DDL3 = "CREATE TABLE numeric ( " +
            "    TestBit bit, " +
            "    TestTinyInt tinyint, " +
            "    TestSmallInt smallint, " +
            "    TestInt int, " +
            "    TestBigInt bigint, " +
            "    TestDecimal decimal(18, 4), " +
            "    TestNumeric numeric(18, 4), " +
            "    TestSmallMoney smallmoney, " +
            "    TestMoney money, " +
            "    TestFloat float, " +
            "    TestReal real, " +
            "    TestIntIdentity int identity, " +
            "  PRIMARY KEY (TestInt) " +
            ")";

    private static String CREATE_DDL4 = "CREATE TABLE string_binary ( " +
            "    TestChar char(10), " +
            "    TestVarChar varchar(10), " +
            "    TestText text, " +
            "    TestNChar nchar(10), " +
            "    TestNVarChar nvarchar(10), " +
            "    TestNText ntext, " +
            "    TestBinary binary(10), " +
            "    TestVarBinary varbinary(10) " +
            ")";

    private static String CREATE_DDL5 = "CREATE TABLE date_time_others ( " +
            "    TestDateTime datetime, " +
            "    TestDateTime2 datetime2, " +
            "    TestDateTimeOffset datetimeoffset, " +
            "    TestImage image, " +
            "    TestUniqueIdentifier uniqueidentifier, " +
            "    TestXml xml, " +
//            "    TestCursor cursor, " +
            "    TestHierarchyId hierarchyid, " +
            "    TestSqlVariant sql_variant, " +
//            "    TestTableType table, " +
            "    TestGeometry geometry, " +
            "    TestGeography geography, " +
            "    TestRowVersion rowversion, " +
//            "    TestTimeStamp timestamp, " +
            "  PRIMARY KEY (TestDateTime) " +
            ")";

    private static String CREATE_DDL6 = "CREATE TABLE user0 ( " +
            "  user_addr varchar(32) NOT NULL, " +
            "  PRIMARY KEY (user_addr) " +
            ")";

    private static String CREATE_DDL7 = "CREATE TABLE user1 ( " +
            "  user_salary decimal(5,2) NOT NULL, " +
            "  PRIMARY KEY (user_salary) " +
            ")";

    private static String CREATE_DDL8 = "CREATE TABLE user2 ( " +
            "  student_id int identity, " +
            "  PRIMARY KEY (student_id) " +
            ")";

    private static String CREATE_DDL9 = "CREATE TABLE user_test ( " +
            "  user_id int DEFAULT NULL, " +
            "  name_t char(1) DEFAULT NULL, " +
            "  useri_date datetime DEFAULT NULL, " +
            "  user_address varchar(32) DEFAULT NULL, " +
            "  userr_salary decimal(5,2) DEFAULT NULL, " +
            "  student_ID int identity, " +
            "  CONSTRAINT user_test_ibfk_1 FOREIGN KEY (user_id) REFERENCES numeric (TestInt), " +
//            "  CONSTRAINT user_test_ibfk_2 FOREIGN KEY (name_t) REFERENCES string_binary (c_name), " +
            "  CONSTRAINT user_test_ibfk_3 FOREIGN KEY (useri_date) REFERENCES date_time_others (TestDateTime), " +
            "  CONSTRAINT user_test_ibfk_4 FOREIGN KEY (user_address) REFERENCES user0 (user_addr), " +
            "  CONSTRAINT user_test_ibfk_5 FOREIGN KEY (userr_salary) REFERENCES user1 (user_salary), " +
            "  CONSTRAINT user_test_ibfk_6 FOREIGN KEY (student_ID) REFERENCES user2 (student_id) " +
            ")";


    @Rule
    public static MSSQLServerContainer mssqlserver = new MSSQLServerContainer(IMAGE)
            .acceptLicense()
            .withPassword(PASSWORD);

    @BeforeAll
    public static void beforeAll() {
        mssqlserver.start();
    }

    /**
     * Get JDBC connection to instance
     *
     * @return {@link Connection}
     * @throws SQLException
     */
    private Connection getConnection(JdbcDatabaseContainer container) throws SQLException {
        return DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }

    /**
     * Generate rosetta compatible Connection model
     * @param container {@link JdbcDatabaseContainer}
     * @return {@link Connection}
     */
    com.adaptivescale.rosetta.common.models.input.Connection getRosettaConnection(JdbcDatabaseContainer container) {
        com.adaptivescale.rosetta.common.models.input.Connection connection = new com.adaptivescale.rosetta.common.models.input.Connection();
        connection.setName("sqlserver-source");
        connection.setUserName(container.getUsername());
        connection.setPassword(container.getPassword());
        connection.setUrl(container.getJdbcUrl());
        connection.setDbType(DB_TYPE);
        return connection;
    }

    /**
     * Get rosetta compatible database model
     *
     * @return
     */
    private Database getDatabaseModel(JdbcDatabaseContainer container) throws Exception {
        com.adaptivescale.rosetta.common.models.input.Connection rosettaConnection = getRosettaConnection(container);
        return SourceGeneratorFactory.sourceGenerator(rosettaConnection).generate(rosettaConnection);
    }


    private Collection<Column> getTableColumns(Database database, String tableName) {
        return database.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst().get().getColumns();
    }

    @Test
    @DisplayName("Prep MSSql Server")
    @Order(0)
    void prep() throws Exception {
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL1);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL2);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL3);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL4);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL5);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL6);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL7);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL8);
        mssqlserver.createConnection("").createStatement().execute(CREATE_DDL9);

    }


    @Test
    @DisplayName("Test extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        assertSame("Comparing table count.", 16, sourceModel.getTables().size());
        assertSame("Comparing Categories table column count.", 4, getTableColumns(sourceModel, "Categories").size());
    }


    @Test
    @DisplayName("Test change finder")
    @Order(2)
    void testDiff() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("Categories")){
                table.setName("Categories_New");
            }
        });
        List<Change<?>> changes = new SQLServerChangeFinder().findChanges(sourceModel, targetModel);
        assertSame("Total changes", changes.size(), 2);
        assertSame("Added table", changes.get(0).getStatus().toString(), "ADD");
        assertSame("Dropped table", changes.get(1).getStatus().toString(), "DROP");
    }


    @Test
    @DisplayName("Test apply changes")
    @Order(3)
    void testApply() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("Categories")){
                table.setName("Categories_New");
            }
        });
        List<Change<?>> changes = DDLFactory.changeFinderForDatabaseType(DB_TYPE).findChanges(targetModel, sourceModel);
        String ddlForChanges = DDLFactory.changeHandler(sourceModel.getDatabaseType()).createDDLForChanges(changes);
        DDLExecutor executor = DDLFactory.executor(getRosettaConnection(mssqlserver), new DriverManagerDriverProvider());
        executor.execute(ddlForChanges);
        Database updatedModel = getDatabaseModel(mssqlserver);
        long categoriesNew = updatedModel.getTables().stream().filter(table -> table.getName().equals("Categories_New")).count();
        long categories = updatedModel.getTables().stream().filter(table -> table.getName().equals("Categories")).count();
        assertSame("Categories_New table exists", 1L, categoriesNew);
        assertSame("Categories table is removed", 0L, categories);
    }

    @Test
    @DisplayName("DDL test for MSSql Server")
    @Order(4)
    void testDDL() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("date_time")){
                table.getColumns().forEach(column -> {
                   switch (column.getName()){
                       case "TestDateTime":
                           assertEquals("","datetime", column.getTypeName());
                           assertEquals("",true, column.isPrimaryKey());
                           break;
                       case "TestDateTime2":
                           assertEquals("","datetime2", column.getTypeName());
                           break;
                       case "TestDateTimeOffset":
                           assertEquals("","datetimeoffset", column.getTypeName());
                           break;
                       case "TestImage":
                           assertEquals("","image", column.getTypeName());
                           break;
                       case "TestUniqueIdentifier":
                           assertEquals("","uniqueidentifier", column.getTypeName());
                           break;
                       case "TestXml":
                           assertEquals("","xml",column.getTypeName());
                           break;
                       case "TestHierarchyId":
                           assertEquals("","hierarchyid", column.getTypeName());
                           break;
                       case "TestSqlVariant":
                           assertEquals("","sql_variant", column.getTypeName());
                           break;
                       case "TestGeometry":
                           assertEquals("","geometry",column.getTypeName());
                           break;
                       case "TestGeography":
                           assertEquals("","geography", column.getTypeName());
                           break;
                       case "TestRowVersion":
                           assertEquals("","rowversion",column.getTypeName());
                           break;
                   }
                });
            }
            if(table.getName().equals("numeric")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "TestBit":
                            assertEquals("","bit", column.getTypeName());
                            break;
                        case "TestTinyInt":
                            assertEquals("","tinyint", column.getTypeName());
                            break;
                        case "TestSmallInt":
                            assertEquals("","smallint", column.getTypeName());
                            break;
                        case "TestInt":
                            assertEquals("","int", column.getTypeName());
                            assertEquals("", true, column.isPrimaryKey());
                            break;
                        case "TestBigInt":
                            assertEquals("","bigint", column.getTypeName());
                            break;
                        case "TestDecimal":
                            assertEquals("","decimal", column.getTypeName());
                            break;
                        case "TestNumeric":
                            assertEquals("","numeric", column.getTypeName());
                            break;
                        case "TestSmallMoney":
                            assertEquals("","smallmoney", column.getTypeName());
                            break;
                        case "TestMoney":
                            assertEquals("","money", column.getTypeName());
                            break;
                        case "TestFloat":
                            assertEquals("","float", column.getTypeName());
                            break;
                        case "TestReal":
                            assertEquals("","real", column.getTypeName());
                            break;
                        case "TestIntIdentity":
                            assertEquals("", "int identity", column.getTypeName());
                            break;
                    }
                });
            }
            if (table.getName().equals("string_binary")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "TestChar":
                            assertEquals("","char", column.getTypeName());
                            break;
                        case "TestVarChar":
                            assertEquals("","varchar", column.getTypeName());
                            break;
                        case "TestText":
                            assertEquals("","text", column.getTypeName());
                            break;
                        case "TestNChar":
                            assertEquals("","nchar", column.getTypeName());
                            break;
                        case "TestNVarChar":
                            assertEquals("","nvarchar", column.getTypeName());
                            break;
                        case "TestNText":
                            assertEquals("","ntext", column.getTypeName());
                            break;
                        case "TestBinary":
                            assertEquals("","binary", column.getTypeName());
                            break;
                        case "TestVarBinary":
                            assertEquals("","varbinary", column.getTypeName());
                            break;
                    }
                });
            }
            if (table.getName().equals("user")){
                table.getColumns().forEach(column -> {
                    if ("user_addr".equals(column.getName())) {
                        assertEquals("", "varchar", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("user1")){
                table.getColumns().forEach(column -> {
                    if ("user_salary".equals(column.getName())) {
                        assertEquals("", "decimal", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("user2")){
                table.getColumns().forEach(column -> {
                    if ("student_id".equals(column.getName())) {
                        assertEquals("", "int identity", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }

        });
    }

    @Test
    @DisplayName("Foreign key constraint test")
    @Order(5)
    void testForeignKey() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if (table.getName().equals("user_test")) {
                table.getColumns().forEach(column -> {
                    switch (column.getName()) {
                        case "user_id":
                            assertEquals("", 1L, column.getForeignKeys().size());
                            break;
                        case "user_address":
                            assertEquals("", 1L, column.getForeignKeys().size());
                            break;
                        case "userr_salary":
                            assertEquals("",1L, column.getForeignKeys().size());
                            break;
                        case "student_ID":
                            assertEquals("",1L, column.getForeignKeys().size());
                            break;
                    }
                });
            }
        });
    }

    @Test
    @DisplayName("Test MSSql Server Assertion tests")
    @Order(6)
    void testAssertionTest() throws Exception {
        Database sourceModel = getDatabaseModel(mssqlserver);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("Categories"))
                table.getColumns().forEach(column -> {
                    if(column.getName().equals("CategoryName")){
                        AssertTest assertTest = new AssertTest();
                        assertTest.setValue("Nick");
                        assertTest.setOperator("=");
                        assertTest.setExpected("0");
                        Tests tests = new Tests();
                        tests.setAssertions(List.of(assertTest));
                        column.setTests(tests);
                    }
                });
        });

        AssertionSqlGenerator assertionSqlGenerator = AssertionSqlGeneratorFactory.generatorFor(getRosettaConnection(mssqlserver));
        DefaultSqlExecution defaultSqlExecution = new DefaultSqlExecution(getRosettaConnection(mssqlserver), new DriverManagerDriverProvider());
        new DefaultAssertTestEngine(assertionSqlGenerator, defaultSqlExecution).run(getRosettaConnection(mssqlserver), targetModel);

    }
}
