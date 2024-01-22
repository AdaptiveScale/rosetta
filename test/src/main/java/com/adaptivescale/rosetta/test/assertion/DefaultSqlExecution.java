package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.ddl.utils.TemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.*;

@Slf4j
public class DefaultSqlExecution implements SqlExecution {
    private final Connection connection;
    private final JDBCDriverProvider driverProvider;

    private Database database;

    private Connection targetConnection;

    public DefaultSqlExecution(Connection connection, JDBCDriverProvider driverProvider) {
        this.connection = connection;
        this.driverProvider = driverProvider;
    }

    public DefaultSqlExecution(Connection connection, JDBCDriverProvider driverProvider, Database database, Connection targetConnection) {
        this.connection = connection;
        this.driverProvider = driverProvider;
        this.database = database;
        this.targetConnection = targetConnection;
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

    @Override
    public String transfer() {
        for (Table table : database.getTables()) {
            doTransfer(table);
        }

        return "OK";
    }

    private String doTransfer(Table table) {
        java.sql.Connection sourceSqlConnection = null;
        java.sql.Connection targetSqlConnection = null;
        String select = table.getExtract();
        String insert = table.getLoad();
        try {
            Driver sourceDriver = driverProvider.getDriver(connection);
            Properties properties = JDBCUtils.setJDBCAuth(connection);
            sourceSqlConnection = sourceDriver.connect(connection.getUrl(), properties);

            Driver targetDriver = driverProvider.getDriver(targetConnection);
            Properties targetProperties = JDBCUtils.setJDBCAuth(targetConnection);
            targetSqlConnection = targetDriver.connect(targetConnection.getUrl(), targetProperties);

            ResultSet resultSet = sourceSqlConnection.createStatement().executeQuery(select);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = resultSet.getMetaData().getColumnCount();

//            List<String> insertStatements = new ArrayList<>();
            Statement statement = targetSqlConnection.createStatement();

            while (resultSet.next()) {
                Map<String, Object> insertParam = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    int index = i+1;

                    Object value = resultSet.getObject(metaData.getColumnName(index));
                    insertParam.put(metaData.getColumnName(index), value);
                }
                String insertStm = TemplateEngine.processString(insert, insertParam);
//                insertStatements.add(insertStm);
                statement.addBatch(insertStm);
//                targetSqlConnection.createStatement().execute(insertStm);
            }

            int[] result = statement.executeBatch();
//            targetSqlConnection.commit();

            return "Rows affected " + result.length;
        } catch (SQLException e) {
            log.error("Can not execute query.", e);
            throw new RuntimeException(e);
        } finally {
            if (sourceSqlConnection != null) {
                try {
                    sourceSqlConnection.close();
                    return "OK";
                } catch (SQLException e) {
                    log.error("Can not close the connection!", e);
                }
            }
        }
//        throw new RuntimeException(String.format("Execution of query: '%s' returns no data", insert));
    }
}
