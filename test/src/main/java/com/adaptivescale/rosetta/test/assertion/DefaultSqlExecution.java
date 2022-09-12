package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Properties;

@Slf4j
public class DefaultSqlExecution implements SqlExecution {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    public DefaultSqlExecution(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    @Override
    public String execute(String sql) {
        java.sql.Connection sqlConnection = null;
        try {
            Driver driver = driverProvider.getDriver(connection);
            Properties properties = JDBCUtils.setJDBCAuth(connection);
            sqlConnection = driver.connect(connection.getUrl(), properties);
            ResultSet execute = sqlConnection.createStatement().executeQuery(sql);
            if (execute.next()) {
                int result = execute.getInt(1);
                return String.valueOf(result);
            }
        } catch (SQLException e) {
            log.error("Can not execute query.", e);
            throw new RuntimeException(e);
        } finally {
            if (sqlConnection != null) {
                try {
                    sqlConnection.close();
                } catch (SQLException e) {
                    log.error("Can not close the connection!", e);
                }
            }
        }
        throw new RuntimeException(String.format("Execution of query: '%s' returns no data", sql));
    }
}
