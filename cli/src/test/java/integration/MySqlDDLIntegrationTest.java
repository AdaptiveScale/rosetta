package integration;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.ddl.change.MySQLChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.DDLExecutor;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
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
    @DisplayName("Test extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = getDatabaseModel(mySQLContainer);
        assertSame("Comparing table count.", 16, sourceModel.getTables().size());
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


}
