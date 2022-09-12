package com.adaptivescale.rosetta.ddl.executor;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlDDLExecutor implements DDLExecutor {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    public MySqlDDLExecutor(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    @Override
    public void execute(String query) throws SQLException {
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);
        properties.setProperty("allowMultiQueries", "true");

        java.sql.Connection jdbcConnection = driver.connect(connection.getUrl(), properties);
        jdbcConnection.createStatement().executeUpdate(query);
        jdbcConnection.close();
    }
}
