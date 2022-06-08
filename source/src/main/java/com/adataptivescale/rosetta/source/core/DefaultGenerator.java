package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;

import java.sql.*;
import java.util.*;


public class DefaultGenerator implements Generator<Database, Target> {

    private final TableExtractor<Collection<Table>, Target, Connection> tableExtractor;
    private final ColumnExtractor<Connection, Collection<Table>> columnsExtractor;

    public DefaultGenerator(TableExtractor<Collection<Table>, Target, Connection> tableExtractor,
                            ColumnExtractor<Connection, Collection<Table>> columnsExtractor) {
        this.tableExtractor = tableExtractor;
        this.columnsExtractor = columnsExtractor;
    }

    @Override
    public Database generate(Target target) throws Exception {
        Driver driver = DriverManager.getDriver(target.getUrl());
        Connection connect = driver.connect(target.getUrl(), new Properties());

        Collection<Table> tables = tableExtractor.extract(target, connect);
        columnsExtractor.extract(connect, tables);

        Database database = new Database();
        database.setName(connect.getMetaData().getDatabaseProductName());
        database.setTables(tables);
        database.setDatabaseType(target.getDbType());
        connect.close();
        return database;
    }
}