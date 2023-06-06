package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.ViewExtractor;

import java.sql.*;
import java.util.*;

public class DefaultGenerator implements Generator<Database, Connection> {
    private final TableExtractor tableExtractor;
    private final ViewExtractor viewExtractor;
    private final ColumnExtractor columnsExtractor;
    private final JDBCDriverProvider driverProvider;

    DefaultGenerator(TableExtractor tableExtractor, ViewExtractor viewExtractor,
                     ColumnExtractor columnsExtractor, JDBCDriverProvider driverProvider) {
        this.tableExtractor = tableExtractor;
        this.viewExtractor = viewExtractor;
        this.columnsExtractor = columnsExtractor;
        this.driverProvider = driverProvider;
    }

    @Override
    public Database generate(Connection connection) throws Exception {
        Driver driver = driverProvider.getDriver(connection);
        Properties properties = JDBCUtils.setJDBCAuth(connection);
        java.sql.Connection connect = driver.connect(connection.getUrl(), properties);

        Collection<Table> tables = (Collection<Table>) tableExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, tables);

        Collection<View> views = (Collection<View>) viewExtractor.extract(connection, connect);
        columnsExtractor.extract(connect, views);

        Database database = new Database();
        database.setName(connect.getMetaData().getDatabaseProductName());
        database.setTables(tables);
        database.setViews(views);
        database.setDatabaseType(connection.getDbType());
        includeData(tables);
        connect.close();
        return database;
    }

    private void includeData(Collection<Table> tables) {
        for (Table table : tables) {
            table.generateExtractSql();
            table.generateLoadSql();
        }
    }
}