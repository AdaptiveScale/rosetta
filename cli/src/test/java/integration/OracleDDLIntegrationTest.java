package integration;


import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import integration.helpers.GenericJDBCContainer;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;

import static org.junit.Assert.assertSame;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OracleDDLIntegrationTest{

    private static String IMAGE = "gvenzl/oracle-xe";
    private static String USERNAME = "ANONYMOUS";
    private static String PASSWORD = "123abcD!";
    private static String DB_TYPE = "oracle";
    private static String DATABASE_NAME = "XE";
    private static int PORT = 1521;

//    private static String CREATE_USER = "CREATE USER TestTable IDENTIFIED BY "+ PASSWORD +";\n";

    private static String CREATE_DDL = "ALTER SESSION SET CURRENT_SCHEMA = TESTUSER;\n" +
            "CREATE TABLE customers (\n" +
            "  customer_id NUMBER(10),\n" +
            "  customer_name VARCHAR2(100),\n" +
            "  customer_email VARCHAR2(100),\n" +
            "  customer_address VARCHAR2(200),\n" +
            "  customer_phone VARCHAR2(20)\n" +
            ");";
    @Rule
    public static OracleContainer oracleContainer = new OracleContainer(DockerImageName.parse(IMAGE)).withDatabaseName(DATABASE_NAME).withStartupTimeout(Duration.ofMinutes(2)).withPassword(PASSWORD).withUsername(USERNAME).withExposedPorts(PORT);

    @BeforeAll
    public static void beforeAll(){oracleContainer.start(); }



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
    @DisplayName("Prep Oracle SQL")
    @Order(0)
    void prep() throws Exception {
//        oracleContainer.createConnection("").createStatement().execute(CREATE_USER);
        oracleContainer.createConnection("").createStatement().execute(CREATE_DDL);
    }

    @Test
    @DisplayName("Test Postgres SQL extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(oracleContainer);
        assertSame("Comparing table count.", 25, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 4, getTableColumns(sourceModel, "customer").size());
    }
}
