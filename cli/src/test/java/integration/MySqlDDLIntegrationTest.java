package integration;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;

import static org.junit.Assert.assertSame;


@Testcontainers
public class MySqlDDLIntegrationTest {

    private static String IMAGE = "sakiladb/mysql:latest";
    private static String USERNAME = "sakila";
    private static String PASSWORD = "p_ssW0rd";
    private static String DATABASE = "sakila";

    private static String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static String DB_TYPE = "mysql";

    private static int PORT = 3306;

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
     * Get rosetta compatible database model
     *
     * @return
     */
    private Database getDatabaseModel(JdbcDatabaseContainer container) throws Exception {
        com.adaptivescale.rosetta.common.models.input.Connection source;
        source = new com.adaptivescale.rosetta.common.models.input.Connection();
        source.setName("mysql-source");
        source.setUserName(container.getUsername());
        source.setPassword(container.getPassword());
        source.setDatabaseName(container.getDatabaseName());
        source.setUrl(container.getJdbcUrl());
        source.setDbType(DB_TYPE);
        return SourceGeneratorFactory.sourceGenerator(source).generate(source);
    }


    private Collection<Column> getTableColumns(Database database, String tableName) {
        return database.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst().get().getColumns();
    }


    @Test
    @DisplayName("Test extract is valid")
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        assertSame("Comparing table count.", 16, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 4, getTableColumns(sourceModel, "actor").size());
    }


    @Test
    @DisplayName("Test change finder is valid")
    void testDiff() throws Exception {
        // TODO - extract source model - create duplicate - modify the duplicate and compare it back with source
        assertSame("example", "example");
    }


    @Test
    @DisplayName("Test change finder is valid")
    void testApply() throws Exception {
        // TODO - extract source model - modify the model and apply it to target
        assertSame("example", "example");
    }


}
