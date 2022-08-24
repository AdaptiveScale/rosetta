package com.adaptivescale.rosetta.ddl.executor;

import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class BigQueryDDLExecutor implements DDLExecutor {
    private final Connection connection;

    public BigQueryDDLExecutor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(String query) throws SQLException {
        Driver driver = DriverManager.getDriver(connection.getUrl());
        Properties properties = JDBCUtils.setJDBCAuth(connection);

        java.sql.Connection jdbcConnection = driver.connect(connection.getUrl(), properties);
        jdbcConnection.createStatement().execute(query);
        jdbcConnection.close();
    }
}
