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
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DB2IntegrationTest {

    private static String IMAGE = "ibmcom/db2";
    private static String USERNAME = "db2inst1";
    private static String PASSWORD = "123456";
    private static String DATABASE = "TESTDB";

    private static String SCHEMA = "WEBSTORE";
    private static String DB_TYPE = "db2";

    private static int PORT = 50000;

    private static String CREATE_SCHEMA = "CREATE SCHEMA \"WEBSTORE\";";

    private static String CREATE_DDL1 = "CREATE TABLE \"WEBSTORE\".\"CUSTOMER\"  (" +
            "  \"C_SALUTATION\" VARCHAR(5 OCTETS) , " +
            "  \"C_LAST_NAME\" VARCHAR(20 OCTETS) , " +
            "  \"C_FIRST_NAME\" VARCHAR(20 OCTETS) , " +
            "  \"C_CUSTOMER_SK\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (  " +
            "    START WITH +1  " +
            "    INCREMENT BY +1  " +
            "    MINVALUE +1  " +
            "    MAXVALUE +2147483647  " +
            "    NO CYCLE  " +
            "    CACHE 20  " +
            "    NO ORDER ) )   " +
            " IN \"USERSPACE1\"  " +
            " ORGANIZE BY ROW; ";


    private static String CREATE_DDL1_PRIMARY_KEY = "ALTER TABLE \"WEBSTORE\".\"CUSTOMER\" " +
            " ADD CONSTRAINT \"CUSTOMER_PK\" PRIMARY KEY" +
            " (\"C_CUSTOMER_SK\")" +
            " NOT ENFORCED;";
    
    private static String CREATE_DDL2 = "CREATE TABLE \"WEBSTORE\".\"MY_TABLE\" ( " +
            "    \"MyBigInt\" BIGINT, " +
            "    \"MyBinary\" BINARY(10), " +
            "    \"MyBlob\" BLOB(10000), " +
            "    \"MyBoolean\" BOOLEAN, " +
            "    \"MyChar\" CHAR(5), " +
            "    \"MyClob\" CLOB(10000), " +
            "    \"MyDate\" DATE, " +
            "    \"MyDecimal\" DECIMAL(10, 3), " +
            "    \"MyDouble\" DOUBLE, " +
            "    \"MyFloat\" FLOAT, " +
            "    \"MyGraphic\" GRAPHIC, " +
            "    \"MyReal\" REAL, " +
            "    \"MySmallInt\" SMALLINT, " +
            "    \"MyTime\" TIME, " +
            "    \"MyTimeStamp\" TIMESTAMP, " +
            "    \"MyVarBinary\" VARBINARY(1000), " +
            "    \"MyVarChar\" VARCHAR(3000), " +
            "    \"MyVarGraphic\" VARGRAPHIC(200), " +
            "    \"MyXml\" XML, " +
            "    \"MyDecimalFloat\" DECFLOAT, " +
            "    \"MyInteger\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (  " +
            "    START WITH +1  " +
            "    INCREMENT BY +1  " +
            "    MINVALUE +1  " +
            "    MAXVALUE +2147483647  " +
            "    NO CYCLE  " +
            "    CACHE 20  " +
            "    NO ORDER ) " +
            ");";

    private static String CREATE_DDL2_PRIMARY_KEY = "ALTER TABLE \"WEBSTORE\".\"MY_TABLE\" " +
            " ADD CONSTRAINT \"MY_TABLE_PK\" PRIMARY KEY" +
            " (\"MyInteger\")" +
            " NOT ENFORCED;";

    public static String CREATE_DDL3 = "CREATE TABLE \"WEBSTORE\".\"NUMERIC\" ( " +
            "    \"MyBigInt\" BIGINT, " +
            "    \"MyDecimal\" DECIMAL(10, 3), " +
            "    \"MyDouble\" DOUBLE, " +
            "    \"MyFloat\" FLOAT, " +
            "    \"MyReal\" REAL, " +
            "    \"MySmallInt\" SMALLINT, " +
            "    \"MyDecimalFloat\" DECFLOAT, " +
            "    \"MyInteger\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (  " +
            "    START WITH +1  " +
            "    INCREMENT BY +1  " +
            "    MINVALUE +1  " +
            "    MAXVALUE +2147483647  " +
            "    NO CYCLE  " +
            "    CACHE 20  " +
            "    NO ORDER ) " +
            ");";

    private static String CREATE_DDL3_PRIMARY_KEY = "ALTER TABLE \"WEBSTORE\".\"NUMERIC\" " +
            " ADD CONSTRAINT \"NUMERIC_PK\" PRIMARY KEY" +
            " (\"MyInteger\")" +
            " NOT ENFORCED;";

    private static String CREATE_DDL4 = "CREATE TABLE \"WEBSTORE\".\"STRING_BINARY\" ( " +
            "    \"MyBinary\" BINARY(10), " +
            "    \"MyBlob\" BLOB(10000), " +
            "    \"MyChar\" CHAR(5), " +
            "    \"MyClob\" CLOB(10000), " +
            "    \"MyVarBinary\" VARBINARY(1000), " +
            "    \"MyVarChar\" VARCHAR(3000), " +
            "    \"MyVarGraphic\" VARGRAPHIC(200), " +
            "    \"MyXml\" XML, " +
            "    \"MyInteger\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (  " +
            "    START WITH +1  " +
            "    INCREMENT BY +1  " +
            "    MINVALUE +1  " +
            "    MAXVALUE +2147483647  " +
            "    NO CYCLE  " +
            "    CACHE 20  " +
            "    NO ORDER ) " +
            ");";

    private static String CREATE_DDL4_PRIMARY_KEY = "ALTER TABLE \"WEBSTORE\".\"STRING_BINARY\" " +
            " ADD CONSTRAINT \"STRING_BINARY_PK\" PRIMARY KEY" +
            " (\"MyInteger\")" +
            " NOT ENFORCED;";

    private static String CREATE_DDL5 = "CREATE TABLE \"WEBSTORE\".\"DATE_TIME_OTHERS\" ( " +
            "    \"MyBoolean\" BOOLEAN, " +
            "    \"MyDate\" DATE, " +
            "    \"MyGraphic\" GRAPHIC, " +
            "    \"MyTime\" TIME, " +
            "    \"MyTimeStamp\" TIMESTAMP, " +
            "    \"MyVarGraphic\" VARGRAPHIC(200), " +
            "    \"MyXml\" XML, " +
            "    \"MyInteger\" INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY (  " +
            "    START WITH +1  " +
            "    INCREMENT BY +1  " +
            "    MINVALUE +1  " +
            "    MAXVALUE +2147483647  " +
            "    NO CYCLE  " +
            "    CACHE 20  " +
            "    NO ORDER ) " +
            ");";

    private static String CREATE_DDL5_PRIMARY_KEY = "ALTER TABLE \"WEBSTORE\".\"DATE_TIME_OTHERS\" " +
            " ADD CONSTRAINT \"DATE_TIME_OTHERS_PK\" PRIMARY KEY" +
            " (\"MyInteger\")" +
            " NOT ENFORCED;";

    private static String CREATE_DDL6 = "CREATE TABLE \"WEBSTORE\".\"USER0\" ( " +
            "  \"user_addr\" VARCHAR(32) NOT NULL, " +
            "  PRIMARY KEY (\"user_addr\") " +
            ")";

    private static String CREATE_DDL7 = "CREATE TABLE \"WEBSTORE\".\"USER1\" ( " +
            "  \"user_salary\" DECIMAL(5,2) NOT NULL, " +
            "  PRIMARY KEY (\"user_salary\") " +
            ")";

    private static String CREATE_DDL8 = "CREATE TABLE \"WEBSTORE\".\"USER2\" ( " +
            "  \"student_id\" INT NOT NULL, " +
            "  PRIMARY KEY (\"student_id\") " +
            ")";

    private static String CREATE_DDL9 = "CREATE TABLE \"WEBSTORE\".\"USER_TEST\" ( " +
            "  user_id INT NOT NULL, " +
            "  name_t CHAR(1), " +
            "  useri_date DATETIME, " +
            "  user_address VARCHAR(32), " +
            "  userr_salary DECIMAL(5,2), " +
            "  student_ID INT, " +
            "  PRIMARY KEY (user_id), " +
            "  FOREIGN KEY (user_address) REFERENCES \"WEBSTORE\".\"USER0\" (\"user_addr\"), " +
            "  FOREIGN KEY (userr_salary) REFERENCES \"WEBSTORE\".\"USER1\" (\"user_salary\"), " +
            "  FOREIGN KEY (student_ID) REFERENCES \"WEBSTORE\".\"USER2\" (\"student_id\") " +
            ")";


    @ClassRule
//    @Rule
    public static Db2Container db2Server = new Db2Container()
            .acceptLicense()
            .withPassword(PASSWORD)
            .withDatabaseName(DATABASE);

    @BeforeAll
    public static void beforeAll() {
        db2Server.start();
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
    @DisplayName("Prep DB2 Server")
    @Order(0)
    void prep() throws Exception {
        db2Server.createConnection("").createStatement().execute(CREATE_SCHEMA);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL1);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL1_PRIMARY_KEY);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL2);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL2_PRIMARY_KEY);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL3);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL3_PRIMARY_KEY);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL4);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL4_PRIMARY_KEY);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL5);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL5_PRIMARY_KEY);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL6);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL7);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL8);
        db2Server.createConnection("").createStatement().execute(CREATE_DDL9);

    }


    @Test
    @DisplayName("Test extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(db2Server);
        assertSame("Comparing table count.", 9, sourceModel.getTables().size());
        assertSame("Comparing Categories table column count.", 4, getTableColumns(sourceModel, "CUSTOMER").size());
    }


    @Test
    @DisplayName("Test change finder")
    @Order(2)
    void testDiff() throws Exception {
        Database sourceModel = getDatabaseModel(db2Server);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("CUSTOMER")){
                table.setName("CUSTOMER_NEW");
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
        Database sourceModel = getDatabaseModel(db2Server);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("CUSTOMER")){
                table.setName("CUSTOMER_NEW");
            }
        });
        List<Change<?>> changes = DDLFactory.changeFinderForDatabaseType(DB_TYPE).findChanges(targetModel, sourceModel);
        String ddlForChanges = DDLFactory.changeHandler(sourceModel.getDatabaseType()).createDDLForChanges(changes);
        DDLExecutor executor = DDLFactory.executor(getRosettaConnection(db2Server), new DriverManagerDriverProvider());
        executor.execute(ddlForChanges);
        Database updatedModel = getDatabaseModel(db2Server);
        long categoriesNew = updatedModel.getTables().stream().filter(table -> table.getName().equals("CUSTOMER_NEW")).count();
        long categories = updatedModel.getTables().stream().filter(table -> table.getName().equals("CUSTOMER")).count();
        assertSame("CUSTOMER_NEW table exists", 1L, categoriesNew);
        assertSame("CUSTOMER table is removed", 0L, categories);
    }

    @Test
    @DisplayName("DDL test for DB2 Server")
    @Order(4)
    void testDDL() throws Exception {
        Database sourceModel = getDatabaseModel(db2Server);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("DATE_TIME_OTHERS")){
                table.getColumns().forEach(column -> {
                   switch (column.getName()){
                       case "MyInteger":
                           assertEquals("","INTEGER", column.getTypeName());
                           assertEquals("",true, column.isPrimaryKey());
                           break;
                       case "MyBoolean":
                           assertEquals("","BOOLEAN", column.getTypeName());
                           break;
                       case "MyDate":
                           assertEquals("","DATE", column.getTypeName());
                           break;
                       case "MyGraphic":
                           assertEquals("","GRAPHIC", column.getTypeName());
                           break;
                       case "MyTime":
                           assertEquals("","TIME", column.getTypeName());
                           break;
                       case "MyTimeStamp":
                           assertEquals("","TIMESTAMP",column.getTypeName());
                           break;
                       case "MyVarGraphic":
                           assertEquals("","VARGRAPHIC", column.getTypeName());
                           break;
                       case "MyXml":
                           assertEquals("","XML", column.getTypeName());
                           break;
                   }
                });
            }
            if(table.getName().equals("NUMERIC")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "MyInteger":
                            assertEquals("","INTEGER", column.getTypeName());
                            assertEquals("",true, column.isPrimaryKey());
                            break;
                        case "MyBigInt":
                            assertEquals("","BIGINT", column.getTypeName());
                            break;
                        case "MyDecimal":
                            assertEquals("","DECIMAL", column.getTypeName());
                            break;
                        case "MyDouble":
                            assertEquals("","DOUBLE", column.getTypeName());
                            break;
                        case "MyFloat":
                            assertEquals("","DOUBLE", column.getTypeName());
                            break;
                        case "MyReal":
                            assertEquals("","REAL", column.getTypeName());
                            break;
                        case "MySmallInt":
                            assertEquals("","SMALLINT", column.getTypeName());
                            break;
                        case "MyDecimalFloat":
                            assertEquals("","DECFLOAT", column.getTypeName());
                            break;
                    }
                });
            }
            if (table.getName().equals("STRING_BINARY")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "MyInteger":
                            assertEquals("","INTEGER", column.getTypeName());
                            assertEquals("",true, column.isPrimaryKey());
                            break;
                        case "MyBinary":
                            assertEquals("","BINARY", column.getTypeName());
                            break;
                        case "MyBlob":
                            assertEquals("","BLOB", column.getTypeName());
                            break;
                        case "MyChar":
                            assertEquals("","CHAR", column.getTypeName());
                            break;
                        case "MyClob":
                            assertEquals("","CLOB", column.getTypeName());
                            break;
                        case "MyVarChar":
                            assertEquals("","VARCHAR", column.getTypeName());
                            break;
                        case "MyVarGraphic":
                            assertEquals("","VARGRAPHIC", column.getTypeName());
                            break;
                        case "MyXml":
                            assertEquals("","XML", column.getTypeName());
                            break;
                    }
                });
            }
            if (table.getName().equals("USER")){
                table.getColumns().forEach(column -> {
                    if ("user_addr".equals(column.getName())) {
                        assertEquals("", "VARCHAR", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("USER1")){
                table.getColumns().forEach(column -> {
                    if ("user_salary".equals(column.getName())) {
                        assertEquals("", "DECIMAL", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("USER2")){
                table.getColumns().forEach(column -> {
                    if ("student_id".equals(column.getName())) {
                        assertEquals("", "INTEGER", column.getTypeName());
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
        Database sourceModel = getDatabaseModel(db2Server);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if (table.getName().equals("USER_TEST")) {
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
    @DisplayName("Test DB2 Server Assertion tests")
    @Order(6)
    void testAssertionTest() throws Exception {
        Database sourceModel = getDatabaseModel(db2Server);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("CUSTOMER"))
                table.getColumns().forEach(column -> {
                    if(column.getName().equals("C_FIRST_NAME")){
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

        AssertionSqlGenerator assertionSqlGenerator = AssertionSqlGeneratorFactory.generatorFor(getRosettaConnection(db2Server));
        DefaultSqlExecution defaultSqlExecution = new DefaultSqlExecution(getRosettaConnection(db2Server), new DriverManagerDriverProvider());
        new DefaultAssertTestEngine(assertionSqlGenerator, defaultSqlExecution).run(getRosettaConnection(db2Server), targetModel);

    }
}