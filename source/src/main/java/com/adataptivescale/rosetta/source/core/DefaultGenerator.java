package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;

import java.sql.*;
import java.util.*;


public class DefaultGenerator implements Generator<Database, com.adaptivescale.rosetta.common.models.input.Connection> {
    public static final String USER_PROPERTY_NAME = "user";
    public static final String PASSWORD_PROPERTY_NAME = "password";
    private final TableExtractor<Collection<Table>, Connection, java.sql.Connection> tableExtractor;
    private final ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor;

     DefaultGenerator(TableExtractor<Collection<Table>, Connection, java.sql.Connection> tableExtractor,
                            ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor) {
        this.tableExtractor = tableExtractor;
        this.columnsExtractor = columnsExtractor;
    }

    @Override
    public Database generate(com.adaptivescale.rosetta.common.models.input.Connection connection) throws Exception {
        Driver driver = DriverManager.getDriver(connection.getUrl());
        java.sql.Connection connect = driver.connect(connection.getUrl(), setAuthProperties(connection));

        Collection<Table> tables = tableExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, tables);

        Database database = new Database();
        database.setName(connect.getMetaData().getDatabaseProductName());
        database.setTables(tables);
        database.setDatabaseType(connection.getDbType());
        connect.close();
        return database;
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