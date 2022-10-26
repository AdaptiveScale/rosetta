package integration;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.DDLFactory;
import com.adaptivescale.rosetta.ddl.change.PostgresChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.DDLExecutor;
import integration.helpers.GenericJDBCContainer;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.Assert.assertSame;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresDDLIntegrationTest {

    private static String IMAGE = "sakiladb/postgres:latest";
    private static String USERNAME = "postgres";
    private static String DATABASE = "sakila";
    private static String SCHEMA = "public";
    private static String PASSWORD = "p_ssW0rd";
    private static String DB_TYPE = "postgres";
    private static String JDBC_URL = "jdbc:postgresql://localhost:{PORT}/sakila";
    private static String CLASS_NAME = "org.postgresql.Driver";
    private static int PORT = 5432;

    private static String DROP_VIEWS = "DROP VIEW actor_info,customer_list,film_list,nicer_but_slower_film_list,sales_by_film_category,sales_by_store,staff_list cascade;";
    @Rule
    public static GenericJDBCContainer container = new GenericJDBCContainer(
            IMAGE, USERNAME,PASSWORD, DATABASE, SCHEMA, DB_TYPE, JDBC_URL, CLASS_NAME, PORT).generateContainer();

    @BeforeAll
    public static void beforeAll() {
        container.getContainer().start();
    }

    @Test
    @DisplayName("Prep Postgres SQL")
    @Order(0)
    void prep() throws Exception {
        container.getContainer().createConnection("").createStatement().execute(DROP_VIEWS);
    }

    @Test
    @DisplayName("Test Postgres SQL extract is valid")
    @Order(1)
    void textExtract() throws Exception {
        Database sourceModel = container.getDatabaseModel();
        assertSame("Comparing table count.", 21, sourceModel.getTables().size());
        assertSame("Comparing actor table column count.", 4, container.getTableColumns(sourceModel, "actor").size());
    }

    @Test
    @DisplayName("Test Postgres SQL change finder")
    @Order(2)
    void testDiff() throws Exception {
        Database sourceModel = container.getDatabaseModel();
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("payment")){
                table.setName("payments");
            }
        });
        List<Change<?>> changes = new PostgresChangeFinder().findChanges(sourceModel, targetModel);
        assertSame("Total changes", changes.size(), 2);
        assertSame("Added table", changes.get(0).getStatus().toString(), "ADD");
        assertSame("Dropped table", changes.get(1).getStatus().toString(), "DROP");
    }


    @Test
    @DisplayName("Test Postgres SQL apply changes")
    @Order(3)
    void testApply() throws Exception {
        Database sourceModel = container.getDatabaseModel();
        ObjectMapper objectMapper = new ObjectMapper();
        Database targetModel = objectMapper.readValue(objectMapper.writeValueAsString(sourceModel), Database.class);
        targetModel.getTables().forEach(table -> {
            if(table.getName().equals("actor")){
                table.setName("actors");
            }
        });
        List<Change<?>> changes = DDLFactory.changeFinderForDatabaseType(DB_TYPE).findChanges(targetModel, sourceModel);
        String ddlForChanges = DDLFactory.changeHandler(sourceModel.getDatabaseType()).createDDLForChanges(changes);
        DDLExecutor executor = DDLFactory.executor(container.getRosettaConnection(), new DriverManagerDriverProvider());
        executor.execute(ddlForChanges);
        Database updatedModel = container.getDatabaseModel();
        long actors = updatedModel.getTables().stream().filter(table -> table.getName().equals("actors")).count();
        long actor = updatedModel.getTables().stream().filter(table -> table.getName().equals("actor")).count();
        assertSame("Actors table exists", 1L, actors);
        assertSame("Actor table is removed", 0L, actor);
    }
}
