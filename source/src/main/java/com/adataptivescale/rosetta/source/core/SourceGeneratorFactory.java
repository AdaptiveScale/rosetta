package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.extractors.BigQueryTableExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;

import java.util.Collection;

public class SourceGeneratorFactory {

    public static Generator<Database, Connection> sourceGenerator(Connection connection) {
        return sourceGenerator(connection, new DriverManagerDriverProvider());
    }

    public static Generator<Database, Connection> sourceGenerator(Connection connection, JDBCDriverProvider driverProvider) {
        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnExtractor<java.sql.Connection, Collection<Table>> columnsExtractor = new ColumnsExtractor(connection);
        if ("bigquery".equals(connection.getDbType())) {
            tablesExtractor = new BigQueryTableExtractor();
            columnsExtractor = new BigQueryColumnsExtractor(connection);
        } else if ("mysql".equals(connection.getDbType())) {
            columnsExtractor = new MySQLColumnsExtractor(connection);
        } else if ("snowflake".equals(connection.getDbType())) {
            columnsExtractor = new SnowflakeColumnsExtractor(connection);
        } else if ("kinetica".equals(connection.getDbType())) {
            columnsExtractor = new KineticaColumnsExtractor(connection);
        } else if ("spanner".equals(connection.getDbType())) {
            columnsExtractor = new SpannerColumnsExtractor(connection);
        }
        return new DefaultGenerator(tablesExtractor, columnsExtractor, driverProvider);
    }
}
