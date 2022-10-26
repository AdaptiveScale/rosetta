package integration.helpers;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adataptivescale.rosetta.source.core.SourceGeneratorFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.util.Collection;

public class GenericJDBCContainer {

    public String image = "";
    private String username = "";
    private String password = "";
    private String database = "";
    private String schema = "";
    private String dbType = "";

    private String jdbcUrl = "";

    private String className = "";
    private int port = 3306;

    private JdbcDatabaseContainer container;

    public GenericJDBCContainer(String image, String username, String password, String database, String schema,
                                String dbType, String jdbcUrl, String className, int port) {
        this.image = image;
        this.username = username;
        this.password = password;
        this.database = database;
        this.schema = schema;
        this.dbType = dbType;
        this.jdbcUrl = jdbcUrl;
        this.className = className;
        this.port = port;
    }

    public GenericJDBCContainer generateContainer() {
        this.container = new JdbcDatabaseContainer(DockerImageName.parse(image)) {
            @Override
            public String getDriverClassName() {
                try {
                    Class.forName(className);
                    return className;
                } catch (ClassNotFoundException e) {
                    return className;
                }
            }

            @Override
            public String getJdbcUrl() {
                return jdbcUrl.replace("{PORT}", getMappedPort(port).toString());
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            protected String getTestQueryString() {
                return "select 1";
            }

            @Override
            public String getDatabaseName() {
                return database;
            }

        };
        this.container.withExposedPorts(port);
        return this;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public JdbcDatabaseContainer getContainer() {
        return container;
    }

    public void setContainer(JdbcDatabaseContainer container) {
        this.container = container;
    }


    /**
     * Generate rosetta compatible Connection model
     * @return {@link Connection}
     */
    public com.adaptivescale.rosetta.common.models.input.Connection getRosettaConnection() {
        com.adaptivescale.rosetta.common.models.input.Connection connection = new com.adaptivescale.rosetta.common.models.input.Connection();
        connection.setName("test-source");
        connection.setUserName(container.getUsername());
        connection.setPassword(container.getPassword());
        connection.setDatabaseName(container.getDatabaseName());
        connection.setSchemaName(schema);
        connection.setUrl(container.getJdbcUrl());
        connection.setDbType(dbType);
        return connection;
    }

    /**
     * Get rosetta compatible database model
     *
     * @return
     */
    public Database getDatabaseModel() throws Exception {
        com.adaptivescale.rosetta.common.models.input.Connection rosettaConnection = getRosettaConnection();
        return SourceGeneratorFactory.sourceGenerator(rosettaConnection).generate(rosettaConnection);
    }

    public Collection<Column> getTableColumns(Database database, String tableName) {
        return database.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst().get().getColumns();
    }
}
