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
        name = "bigquery",
        type = RosettaModuleTypes.DDL_EXECUTOR
)
public class BigQueryDDLExecutor implements DDLExecutor {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    public BigQueryDDLExecutor(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    @Override
    public void execute(String query) throws SQLException {
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);

        java.sql.Connection jdbcConnection = driver.connect(connection.getUrl(), properties);
        jdbcConnection.createStatement().execute(query);
        jdbcConnection.close();
    }
}
