package com.adaptivescale.rosetta.ddl.executor;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@RosettaModule(
        name = "spanner",
        type = RosettaModuleTypes.DDL_EXECUTOR
)
public class SpannerDDLExecutor implements DDLExecutor {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    public SpannerDDLExecutor(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    /**
     * Sanitized query for Spanner and separates into statements
     * @param query {@link String}
     * @return {@link List<String>} query statements list
     */
    private List<String> parseQuery(String query) {
        String sanitizedQuery = query.replaceAll("\r", "").replaceAll(";", ";\n");
        String[] split = sanitizedQuery.split(";");
        List<String> queryParts = new ArrayList<>();
        for (String s : split) {
            if(!s.trim().isEmpty()){
                queryParts.add(s);
            }
        }
        return queryParts;
    }

    @Override
    public void execute(String query) throws SQLException {

        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);

        java.sql.Connection jdbcConnection = driver.connect(connection.getUrl(), properties);
        // Spanner does not allow multiple DDL statements in single query
        List<String> queryParts = parseQuery(query);
        for (String queryPart : queryParts) {
            jdbcConnection.createStatement().executeUpdate(queryPart);
        }
        jdbcConnection.close();

    }
}
