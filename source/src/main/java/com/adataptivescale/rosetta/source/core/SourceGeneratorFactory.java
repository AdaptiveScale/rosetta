package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;

import java.util.Collection;

public class SourceGeneratorFactory {

    public static Generator<Database, Connection> sourceGenerator(Connection connection) {
        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor = null;
        if ("bigquery".equals(connection.getDbType())) {
            columnsExtractor = new BigQueryColumnsExtractor(connection);
        } else if ("mysql".equals(connection.getDbType())) {
            columnsExtractor = new MySQLColumnsExtractor(connection);
        } else if ("snowflake".equals(connection.getDbType())) {
            columnsExtractor = new SnowflakeColumnsExtractor(connection);
        } else {
            columnsExtractor = new ColumnsExtractor(connection);
        }
        return new DefaultGenerator(tablesExtractor, columnsExtractor);
    }
}
