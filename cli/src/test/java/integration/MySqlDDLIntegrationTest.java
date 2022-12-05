package integration;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.test.Tests;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.ddl.change.MySQLChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.DDLExecutor;
import com.adaptivescale.rosetta.test.assertion.AssertionSqlGenerator;
import com.adaptivescale.rosetta.test.assertion.DefaultAssertTestEngine;
import com.adaptivescale.rosetta.test.assertion.DefaultSqlExecution;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import integration.helpers.GenericJDBCContainer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
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
public class MySqlDDLIntegrationTest {

    private static String IMAGE = "sakiladb/mysql:latest";
    private static String USERNAME = "sakila";
    private static String PASSWORD = "p_ssW0rd";
    private static String DATABASE = "sakila";
    private static String DB_TYPE = "mysql";

    private static int PORT = 3306;

    private static String CREATE_DDL = "CREATE TABLE `date_time` (\n" +
            "  `c_date` date DEFAULT NULL,\n" +
            "  `c_year` year NOT NULL,\n" +
            "  `c_time` time NOT NULL,\n" +
            "  `c_timest` timestamp NOT NULL,\n" +
            "  `c_datetime` datetime NOT NULL,\n" +
            "  `user_date` date NOT NULL,\n" +
            "  PRIMARY KEY (`user_date`)\n" +
            ")";

    public static String CREATE_DDL1 = "CREATE TABLE `numeric` (\n" +
            "  `c_bigint` bigint DEFAULT NULL,\n" +
            "  `c_boolean` tinyint(1) DEFAULT NULL,\n" +
            "  `c_decimal` decimal(10,0) NOT NULL,\n" +
            "  `c_float` float NOT NULL,\n" +
            "  `c_integer` int NOT NULL,\n" +
            "  `mediumint_unsigned` mediumint unsigned NOT NULL,\n" +
            "  `c_smallint` smallint NOT NULL,\n" +
            "  `smallint_unsigned` smallint unsigned NOT NULL,\n" +
            "  `tinyint_unsigned` tinyint unsigned NOT NULL,\n" +
            "  `c_tinyint` tinyint NOT NULL,\n" +
            "  `c_mediumint` mediumint NOT NULL,\n" +
            "  `c_int` int NOT NULL,\n" +
            "  `int_unsigned` int unsigned NOT NULL,\n" +
            "  `c_double` double NOT NULL,\n" +
            "  `c_bool` tinyint(1) DEFAULT NULL,\n" +
            "  `useri_id` int NOT NULL,\n" +
            "  PRIMARY KEY (`useri_id`)\n" +
            ")";

    private static String CREATE_DDL2 = "CREATE TABLE `string_binary` (\n" +
            "  `c_varchar` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,\n" +
            "  `c_character` char(1) NOT NULL,\n" +
            "  `c_binary` binary(1) NOT NULL,\n" +
            "  `c_blob` blob,\n" +
            "  `c_varbin` varbinary(100) DEFAULT NULL,\n" +
            "  `c_tinyblob` tinyblob,\n" +
            "  `c_tinytext` tinytext,\n" +
            "  `c_text` text,\n" +
            "  `c_medtext` mediumtext,\n" +
            "  `c_medblob` mediumblob,\n" +
            "  `c_longtext` longtext,\n" +
            "  `c_longblob` longblob,\n" +
            "  `c_longvch` mediumtext,\n" +
            "  `VCH_nosize` varchar(1) DEFAULT NULL,\n" +
            "  `c_name` char(1) NOT NULL,\n" +
            "  PRIMARY KEY (`c_name`)\n" +
            ")";

    private static String CREATE_DDL3 = "CREATE TABLE `user` (\n" +
            "  `user_addr` varchar(32) NOT NULL,\n" +
            "  PRIMARY KEY (`user_addr`)\n" +
            ")";

    private static String CREATE_DDL4 = "CREATE TABLE `user1` (\n" +
            "  `user_salary` decimal(5,2) NOT NULL,\n" +
            "  PRIMARY KEY (`user_salary`)\n" +
            ")";

    private static String CREATE_DDL5 = "CREATE TABLE `user2` (\n" +
            "  `student_id` bigint unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  PRIMARY KEY (`student_id`),\n" +
            "  UNIQUE KEY `student_id` (`student_id`)\n" +
            ")";

    private static String CREATE_DDL6 = "CREATE TABLE `user_test` (\n" +
            "  `user_id` int DEFAULT NULL,\n" +
            "  `name_t` char(1) DEFAULT NULL,\n" +
            "  `useri_date` date DEFAULT NULL,\n" +
            "  `user_address` varchar(32) DEFAULT NULL,\n" +
            "  `userr_salary` decimal(5,2) DEFAULT NULL,\n" +
            "  `student_ID` bigint unsigned NOT NULL AUTO_INCREMENT,\n" +
            "  UNIQUE KEY `student_ID` (`student_ID`),\n" +
            "  KEY `user_id` (`user_id`),\n" +
            "  KEY `name_t` (`name_t`),\n" +
            "  KEY `useri_date` (`useri_date`),\n" +
            "  KEY `user_address` (`user_address`),\n" +
            "  KEY `userr_salary` (`userr_salary`),\n" +
            "  CONSTRAINT `user_test_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `numeric` (`useri_id`),\n" +
            "  CONSTRAINT `user_test_ibfk_2` FOREIGN KEY (`name_t`) REFERENCES `string_binary` (`c_name`),\n" +
            "  CONSTRAINT `user_test_ibfk_3` FOREIGN KEY (`useri_date`) REFERENCES `date_time` (`user_date`),\n" +
            "  CONSTRAINT `user_test_ibfk_4` FOREIGN KEY (`user_address`) REFERENCES `user` (`user_addr`),\n" +
            "  CONSTRAINT `user_test_ibfk_5` FOREIGN KEY (`userr_salary`) REFERENCES `user1` (`user_salary`),\n" +
            "  CONSTRAINT `user_test_ibfk_6` FOREIGN KEY (`student_ID`) REFERENCES `user2` (`student_id`)\n" +
            ")";

    @Rule
    public static MySQLContainer mySQLContainer = new MySQLContainer(DockerImageName.parse(IMAGE)
            .asCompatibleSubstituteFor("mysql")
    ).withPassword(PASSWORD).withUsername(USERNAME).withDatabaseName(DATABASE).withConfigurationOverride("");


    @BeforeAll
    public static void beforeAll() {
        mySQLContainer.start();
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
        connection.setName("mysql-source");
        connection.setUserName(container.getUsername());
        connection.setPassword(container.getPassword());
        connection.setDatabaseName(container.getDatabaseName());
        connection.setSchemaName("sakila");
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
    @DisplayName("Prep MySQL")
    @Order(0)
    void prep() throws Exception {
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL1);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL2);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL3);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL4);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL5);
        mySQLContainer.createConnection("").createStatement().execute(CREATE_DDL6);
    }


    @Test
    @DisplayName("Test extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        assertSame("Comparing table count.", 23, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 4, getTableColumns(sourceModel, "actor").size());
    }


    @Test
    @DisplayName("Test change finder")
    @Order(2)
    void testDiff() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("actor")){
                table.setName("actors");
            }
        });
        List<Change<?>> changes = new MySQLChangeFinder().findChanges(sourceModel, targetModel);
        assertSame("Total changes", changes.size(), 2);
        assertSame("Added table", changes.get(0).getStatus().toString(), "ADD");
        assertSame("Dropped table", changes.get(1).getStatus().toString(), "DROP");
    }


    @Test
    @DisplayName("Test apply changes")
    @Order(3)
    @Ignore
    @Disabled
    void testApply() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("actor")){
                table.setName("actors");
            }
        });
        List<Change<?>> changes = DDLFactory.changeFinderForDatabaseType(DB_TYPE).findChanges(targetModel, sourceModel);
        String ddlForChanges = DDLFactory.changeHandler(sourceModel.getDatabaseType()).createDDLForChanges(changes);
        DDLExecutor executor = DDLFactory.executor(getRosettaConnection(mySQLContainer), new DriverManagerDriverProvider());
        executor.execute(ddlForChanges);
        Database updatedModel = getDatabaseModel(mySQLContainer);
        long actors = updatedModel.getTables().stream().filter(table -> table.getName().equals("actors")).count();
        long actor = updatedModel.getTables().stream().filter(table -> table.getName().equals("actor")).count();
        assertSame("Actors table exists", 1L, actors);
        assertSame("Actor table is removed", 0L, actor);
    }

    @Test
    @DisplayName("DDL test for MySQL")
    @Order(4)

    void testDDL() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("date_time")){
                table.getColumns().forEach(column -> {
                   switch (column.getName()){
                       case "c_date":
                           assertEquals("","DATE", column.getTypeName());
                           break;
                       case "c_year":
                           assertEquals("","YEAR", column.getTypeName());
                           break;
                       case "c_time":
                           assertEquals("","TIME", column.getTypeName());
                           break;
                       case "c_timest":
                           assertEquals("","TIMESTAMP", column.getTypeName());
                           break;
                       case "c_datetime":
                           assertEquals("","DATETIME", column.getTypeName());
                           break;
                       case "user_date":
                           assertEquals("","DATE",column.getTypeName());
                           assertEquals("",true, column.isPrimaryKey());
                           break;
                   }
                });
            }
            if(table.getName().equals("numeric")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "c_bigint":
                            assertEquals("","BIGINT", column.getTypeName());
                            break;
                        case "c_boolean":
                            assertEquals("","BIT", column.getTypeName());
                            break;
                        case "c_decimal":
                            assertEquals("","DECIMAL", column.getTypeName());
                            break;
                        case "c_float":
                            assertEquals("","FLOAT", column.getTypeName());
                            break;
                        case "c_integer":
                            assertEquals("","INT", column.getTypeName());
                            break;
                        case "mediumint_unsigned":
                            assertEquals("","MEDIUMINT UNSIGNED", column.getTypeName());
                            break;
                        case "c_smallint":
                            assertEquals("","SMALLINT", column.getTypeName());
                            break;
                        case "smallint_unsigned":
                            assertEquals("","SMALLINT UNSIGNED", column.getTypeName());
                            break;
                        case "tinyint_unsigned":
                            assertEquals("","TINYINT UNSIGNED", column.getTypeName());
                            break;
                        case "c_tinyint":
                            assertEquals("","TINYINT", column.getTypeName());
                            break;
                        case "c_mediumint":
                            assertEquals("","MEDIUMINT", column.getTypeName());
                            break;
                        case "c_int":
                            assertEquals("", "INT", column.getTypeName());
                            break;
                        case "int_usnigned":
                            assertEquals("", "INT UNSIGNED", column.getTypeName());
                            break;
                        case "c_double":
                            assertEquals("", "DOUBLE", column.getTypeName());
                            break;
                        case "c_bool":
                            assertEquals("","BIT", column.getTypeName());
                            break;
                        case "useri_id":
                            assertEquals("","INT", column.getTypeName());
                            assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("string_binary")){
                table.getColumns().forEach(column -> {
                    switch (column.getName()){
                        case "c_varchar":
                            assertEquals("","VARCHAR", column.getTypeName());
                            break;
                        case "c_character":
                            assertEquals("","CHAR", column.getTypeName());
                            break;
                        case "c_binary":
                            assertEquals("","BINARY", column.getTypeName());
                            break;
                        case "c_blob":
                            assertEquals("","BLOB", column.getTypeName());
                            break;
                        case "c_varbin":
                            assertEquals("","VARBINARY", column.getTypeName());
                            break;
                        case "c_tinyblob":
                            assertEquals("","TINYBLOB", column.getTypeName());
                            break;
                        case "c_text":
                            assertEquals("","TEXT", column.getTypeName());
                            break;
                        case "c_medtext":
                            assertEquals("","MEDIUMTEXT", column.getTypeName());
                            break;
                        case "c_medblob":
                            assertEquals("","MEDIUMBLOB", column.getTypeName());
                            break;
                        case "c_longtext":
                            assertEquals("","LONGTEXT", column.getTypeName());
                            break;
                        case "c_longblob":
                            assertEquals("","LONGBLOB", column.getTypeName());
                            break;
                        case "c_longvch":
                            assertEquals("","MEDIUMTEXT", column.getTypeName());
                            break;
                        case "VCH_nosize":
                            assertEquals("","VARCHAR", column.getTypeName());
                            break;
                        case "c_name":
                            assertEquals("","CHAR", column.getTypeName());
                            assertEquals("",true, column.isPrimaryKey());
                            break;
                    }
                });
            }
            if (table.getName().equals("user")){
                table.getColumns().forEach(column -> {
                    if ("user_addr".equals(column.getName())) {
                        assertEquals("", "VARCHAR", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("user1")){
                table.getColumns().forEach(column -> {
                    if ("user_salary".equals(column.getName())) {
                        assertEquals("", "DECIMAL", column.getTypeName());
                        assertEquals("", true, column.isPrimaryKey());
                    }
                });
            }
            if (table.getName().equals("user2")){
                table.getColumns().forEach(column -> {
                    if ("student_id".equals(column.getName())) {
                        assertEquals("", "BIGINT UNSIGNED", column.getTypeName());
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
        Database sourceModel = getDatabaseModel(mySQLContainer);
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if (table.getName().equals("user_test")) {
                table.getColumns().forEach(column -> {
                    switch (column.getName()) {
                        case "user_id":
                            assertEquals("", 1L, column.getForeignKeys().size());
                            break;
                        case "name_t":
                            assertEquals("", 1L, column.getForeignKeys().size());
                            break;
                        case "useri_date":
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
}
