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
import com.adaptivescale.rosetta.test.assertion.generator.AssertionSqlGeneratorFactory;
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
public class KineticaDDLIntegrationTest {

    private static String IMAGE = "kinetica/kinetica-intel:latest";
    private static String USERNAME = "";
    private static String DATABASE = "kinetica";
    private static String SCHEMA = "ki_catalog";
    private static String PASSWORD = "";
    private static String DB_TYPE = "kinetica";
    private static String JDBC_URL = "jdbc:kinetica:URL=http://localhost:{PORT};CombinePrepareAndExecute=1;Schema={SCHEMA};currentSchema={SCHEMA}";
    private static String CLASS_NAME = "org.kinetica.jdbc.Driver";
    private static int PORT = 9191;

    private static String CREATE_DDL = "CREATE TABLE \"numerics\"\n" +
            "(\n" +
            "    \"c_integer\" INTEGER (primary_key) NOT NULL,\n" +
            "    \"c_int8\" TINYINT NOT NULL,\n" +
            "    \"c_int16\" SMALLINT NOT NULL,\n" +
            "    \"c_float\" REAL NOT NULL,\n" +
            "    \"c_double\" DOUBLE NOT NULL,\n" +
            "    \"c_long\" BIGINT NOT NULL\n" +
            ")\n" +
            "TIER STRATEGY (\n" +
            "( ( VRAM 1, RAM 5, DISK0 5, PERSIST 5 ) )\n" +
            ");";

    @Rule
    public static GenericJDBCContainer container = new GenericJDBCContainer(
            IMAGE, USERNAME, DATABASE, SCHEMA, PASSWORD, DB_TYPE, JDBC_URL, CLASS_NAME, PORT).generateContainer();

    @BeforeAll
    public static void beforeAll(){
        container.getContainer().start();
    }

    @Test
    @DisplayName("")
    @Order(0)
    void prep() throws Exception{
        container.getContainer().createConnection("").createStatement().execute(CREATE_DDL);
    }

    @Test
    @DisplayName("")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = container.getDatabaseModel();
        assertSame("Comparing table count.", 25, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 4, container.getTableColumns(sourceModel, "actor").size());
    }

}
