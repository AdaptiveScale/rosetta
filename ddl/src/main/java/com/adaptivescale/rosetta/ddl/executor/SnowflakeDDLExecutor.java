package com.adaptivescale.rosetta.ddl.executor;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

@RosettaModule(
        name = "snowflake",
        type = RosettaModuleTypes.DDL_EXECUTOR
)
public class SnowflakeDDLExecutor implements DDLExecutor {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    public SnowflakeDDLExecutor(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    @Override
    public void execute(String query) throws SQLException {
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);

        java.sql.Connection jdbcConnection = driver.connect(connection.getUrl(), properties);
        jdbcConnection.createStatement().execute("alter session set MULTI_STATEMENT_COUNT = 0;");
        if (query != null && !query.isEmpty()) {
            jdbcConnection.createStatement().executeUpdate(query);
        }
        jdbcConnection.close();
    }
}
