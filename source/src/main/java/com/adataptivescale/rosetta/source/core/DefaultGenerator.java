package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;

import java.sql.*;
import java.util.*;

public class DefaultGenerator implements Generator<Database, Connection> {
    private final TableExtractor<Collection<Table>, Connection, java.sql.Connection> tableExtractor;
    private final ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor;
    private final JDBCDriverProvider driverProvider;

    DefaultGenerator(TableExtractor<Collection<Table>, Connection, java.sql.Connection> tableExtractor,
                     ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor, JDBCDriverProvider driverProvider) {
        this.tableExtractor = tableExtractor;
        this.columnsExtractor = columnsExtractor;
        this.driverProvider = driverProvider;
    }

    @Override
    public Database generate(Connection connection) throws Exception {
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);
        java.sql.Connection connect = driver.connect(connection.getUrl(), properties);

        Collection<Table> tables = tableExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, tables);

        Database database = new Database();
        database.setName(connect.getMetaData().getDatabaseProductName());
        database.setTables(tables);
        database.setDatabaseType(connection.getDbType());
        connect.close();
        return database;
    }
}