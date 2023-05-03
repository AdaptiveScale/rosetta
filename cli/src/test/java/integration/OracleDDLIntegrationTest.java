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
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OracleDDLIntegrationTest{

    private static String IMAGE = "gvenzl/oracle-xe";
    private static String USERNAME = "system";
    private static String PASSWORD = "123abcD";
    private static String DB_TYPE = "oracle";
    private static String DATABASE_NAME = "XE2";
    private static String DEFAULT_SCHEMA = "TEST";
    private static int PORT = 1521;

    private static String CREATE_USER = "CREATE USER TESTUSER IDENTIFIED BY \"123abcD\"";

    private static String GRANT_DDL = "GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO TESTUSER";

    private static String SET_CURRENT_SCHEMA = "ALTER SESSION SET CURRENT_SCHEMA = TEST";

    private static String CREATE_DDL = "CREATE table TEST.EMPLOYEE " +
            "(employee_id NUMBER(4) NOT NULL, employee_name VARCHAR2 (30), employment_length VARCHAR2 (40))";

    private static String CREATE_DDL2 = "create table TEST.numerics (\n" +
            "    c_number number(10,2),\n" +
            "    c_float float(5),\n" +
            "    c_long long,\n" +
            "    c_binaryfloat binary_float,\n" +
            "    c_binarydouble binary_double\n" +
            ")";
    private static String CREATE_DDL3 = "create table TEST.time_date (\n" +
            "    c_date date,\n" +
            "    c_timerstamp timestamp,\n" +
            "    c_timestampTZ timestamp with time zone,\n" +
            "    c_interval Interval year to month,\n" +
            "    c_interval2 interval day to second\n" +
            ")";
    private static String CREATE_DDL4 = "create table TEST.strings_binary (\n" +
            "    c_char char,\n" +
            "    c_nchar nchar,\n" +
            "    c_varchar varchar(12),\n" +
            "    c_nvarchar nvarchar2(2),\n" +
            "    c_clob clob,\n" +
            "    c_nclob nclob,\n" +
            "    c_blob blob,\n" +
            "    c_bfile bfile,\n" +
            "    c_raw raw(10),\n" +
            "    c_longraw long raw,\n" +
            "    c_rowid rowid\n" +
            ")";

    private static String CREATE_DDL6 = "CREATE TABLE TEST.user0 ( " +
            "  user_addr varchar(32) NOT NULL, " +
            "  PRIMARY KEY (user_addr) " +
            ")";

    private static String CREATE_DDL7 = "CREATE TABLE TEST.user1 ( " +
            "  user_salary number(5,2) NOT NULL, " +
            "  PRIMARY KEY (user_salary) " +
            ")";

    private static String CREATE_DDL8 = "CREATE TABLE TEST.user2 ( " +
            "  student_id int, " +
            "  PRIMARY KEY (student_id) " +
            ")";

    private static String CREATE_DDL9 = "CREATE TABLE user_test ( " +
            "  user_id int DEFAULT NULL, " +
            "  name_t char(1) DEFAULT NULL, " +
            "  useri_date timestamp DEFAULT NULL, " +
            "  user_address varchar(32) DEFAULT NULL, " +
            "  userr_salary number(5,2) DEFAULT NULL, " +
            "  student_ID int, " +
//            "  CONSTRAINT user_test_ibfk_1 FOREIGN KEY (user_id) REFERENCES numeric (TestInt), " +
//            "  CONSTRAINT user_test_ibfk_2 FOREIGN KEY (name_t) REFERENCES string_binary (c_name), " +
//            "  CONSTRAINT user_test_ibfk_3 FOREIGN KEY (useri_date) REFERENCES date_time_others (TestDateTime), " +
            "  CONSTRAINT user_test_ibfk_4 FOREIGN KEY (user_address) REFERENCES user0 (user_addr), " +
            "  CONSTRAINT user_test_ibfk_5 FOREIGN KEY (userr_salary) REFERENCES user1 (user_salary), " +
            "  CONSTRAINT user_test_ibfk_6 FOREIGN KEY (student_ID) REFERENCES user2 (student_id) " +
            ")";

    @Rule
    public static OracleContainer oracleContainer = new OracleContainer(DockerImageName.parse(IMAGE))
        .withDatabaseName(DATABASE_NAME)
        .withStartupTimeout(Duration.ofMinutes(3))
        .withPassword(PASSWORD)
        .withExposedPorts(PORT);

    @BeforeAll
    public static void beforeAll() {
        oracleContainer.start();
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
        connection.setName("oracle-source");
        connection.setDatabaseName(DATABASE_NAME);
        connection.setSchemaName(DEFAULT_SCHEMA);
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
    @DisplayName("Prep Oracle SQL")
    @Order(0)
    void prep() throws Exception {
        oracleContainer.createConnection("").createStatement().execute(SET_CURRENT_SCHEMA);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL2);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL3);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL4);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL6);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL7);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL8);
        oracleContainer.createConnection("").createStatement().executeUpdate(CREATE_DDL9);
    }

    @Test
    @DisplayName("Test Postgres SQL extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(oracleContainer);
        assertSame("Comparing table count.", 8, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 3, getTableColumns(sourceModel, "EMPLOYEE").size());
    }

    @Test
    @DisplayName("Test change finder")
    @Order(2)
    void testDiff() throws Exception {
        Database sourceModel = getDatabaseModel(oracleContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("EMPLOYEE")){
                table.setName("EMPLOYEE_NEW");
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
        Database sourceModel = getDatabaseModel(oracleContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("EMPLOYEE")){
                table.setName("EMPLOYEE_NEW");
            }
        });
        List<Change<?>> changes = DDLFactory.changeFinderForDatabaseType(DB_TYPE).findChanges(targetModel, sourceModel);
        String ddlForChanges = DDLFactory.changeHandler(sourceModel.getDatabaseType()).createDDLForChanges(changes);
        DDLExecutor executor = DDLFactory.executor(getRosettaConnection(oracleContainer), new DriverManagerDriverProvider());
        executor.execute(ddlForChanges);
        Database updatedModel = getDatabaseModel(oracleContainer);
        long employeeNew = updatedModel.getTables().stream().filter(table -> table.getName().equals("EMPLOYEE_NEW")).count();
        long employee = updatedModel.getTables().stream().filter(table -> table.getName().equals("EMPLOYEE")).count();
        assertSame("EMPLOYEE_NEW table exists", 1L, employeeNew);
        assertSame("EMPLOYEE table is removed", 0L, employee);
    }

    @Test
    @DisplayName("DDL test for Oracle DB")
    @Order(4)
    void testDDL() throws Exception {
        Database sourceModel = getDatabaseModel(oracleContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("date_time")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "c_date":
                            assertEquals("","date", column.getTypeName());
//                            assertEquals("",true, column.isPrimaryKey());
                            break;
                        case "c_timerstamp":
                            assertEquals("","timestamp", column.getTypeName());
                            break;
                        case "c_timestampTZ":
                            assertEquals("","timestamp with time zone", column.getTypeName());
                            break;
                        case "c_interval":
                            assertEquals("","Interval year to month", column.getTypeName());
                            break;
                        case "c_interval2":
                            assertEquals("","interval day to second", column.getTypeName());
                            break;
                    }
                });
            }
            if(table.getName().equals("numeric")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "c_number":
                            assertEquals("","number", column.getTypeName());
                            assertEquals("",10, column.getPrecision());
                            assertEquals("",2, column.getScale());
                            break;
                        case "c_float":
                            assertEquals("","float", column.getTypeName());
                            break;
                        case "c_long":
                            assertEquals("","long", column.getTypeName());
                            break;
                        case "c_binaryfloat":
                            assertEquals("","binary_float", column.getTypeName());
//                            assertEquals("", true, column.isPrimaryKey());
                            break;
                        case "c_binarydouble":
                            assertEquals("","binary_double", column.getTypeName());
                            break;
                    }
                });
            }
            if (table.getName().equals("string_binary")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "c_char":
                            assertEquals("","char", column.getTypeName());
                            break;
                        case "c_nchar":
                            assertEquals("","nchar", column.getTypeName());
                            break;
                        case "c_varchar":
                            assertEquals("","varchar", column.getTypeName());
                            assertEquals("",12, column.getColumnDisplaySize());
                            break;
                        case "c_nvarchar":
                            assertEquals("","nvarchar2", column.getTypeName());
                            assertEquals("",2, column.getColumnDisplaySize());
                            break;
                        case "c_clob":
                            assertEquals("","clob", column.getTypeName());
                            break;
                        case "c_nclob":
                            assertEquals("","nclob", column.getTypeName());
                            break;
                        case "c_blob":
                            assertEquals("","blob", column.getTypeName());
                            break;
                        case "c_bfile":
                            assertEquals("","c_bfile", column.getTypeName());
                            break;
                        case "c_raw":
                            assertEquals("","raw", column.getTypeName());
                            break;
                        case "c_longraw":
                            assertEquals("","long raw", column.getTypeName());
                            break;
                        case "c_rowid":
                            assertEquals("","rowid", column.getTypeName());
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
        Database sourceModel = getDatabaseModel(oracleContainer);
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
    @DisplayName("Test Oracle DB Assertion tests")
    @Order(6)
    void testAssertionTest() throws Exception {
        Database sourceModel = getDatabaseModel(oracleContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("EMPLOYEE"))
                table.getColumns().forEach(column -> {
                    if(column.getName().equals("employee_name")){
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

        AssertionSqlGenerator assertionSqlGenerator = AssertionSqlGeneratorFactory.generatorFor(getRosettaConnection(oracleContainer));
        DefaultSqlExecution defaultSqlExecution = new DefaultSqlExecution(getRosettaConnection(oracleContainer), new DriverManagerDriverProvider());
        new DefaultAssertTestEngine(assertionSqlGenerator, defaultSqlExecution).run(getRosettaConnection(oracleContainer), targetModel);

    }
}
