package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.models.input.Connection;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Properties;

@Slf4j
public class DefaultSqlExecution implements SqlExecution {
    public static final String USER_PROPERTY_NAME = "user";
    public static final String PASSWORD_PROPERTY_NAME = "password";

    private final Connection connection;

    public DefaultSqlExecution(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String execute(String sql) {
        java.sql.Connection sqlConnection = null;
        try {
            Driver driver = DriverManager.getDriver(connection.getUrl());
            sqlConnection = driver.connect(connection.getUrl(), setAuthProperties(connection));
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

    private Properties setAuthProperties(Connection connection) {
        Properties properties = new Properties();
        if (connection.getUserName() != null) {
            properties.setProperty(USER_PROPERTY_NAME, connection.getUserName());
        }
        if (connection.getPassword() != null) {
            properties.setProperty(PASSWORD_PROPERTY_NAME, connection.getPassword());
        }
        return properties;
    }
}
