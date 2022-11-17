package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.extractors.column.*;
import com.adataptivescale.rosetta.source.core.extractors.table.DefaultTablesExtractor;
import com.adataptivescale.rosetta.source.core.extractors.view.BigQueryViewExtractor;
import com.adataptivescale.rosetta.source.core.extractors.view.DefaultViewExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;
import com.adataptivescale.rosetta.source.core.interfaces.ViewExtractor;

public class SourceGeneratorFactory {

    public static Generator<Database, Connection> sourceGenerator(Connection connection) {
        return sourceGenerator(connection, new DriverManagerDriverProvider());
    }

    public static Generator<Database, Connection> sourceGenerator(Connection connection, JDBCDriverProvider driverProvider) {
        DefaultTablesExtractor tablesExtractor = new DefaultTablesExtractor();
        ColumnExtractor columnsExtractor;
        ViewExtractor viewExtractor = new DefaultViewExtractor();
        if ("bigquery".equals(connection.getDbType())) {
            columnsExtractor = new BigQueryColumnsExtractor(connection);
            viewExtractor = new BigQueryViewExtractor();
        } else if ("mysql".equals(connection.getDbType())) {
            columnsExtractor = new MySQLColumnsExtractor(connection);
        } else if ("snowflake".equals(connection.getDbType())) {
            columnsExtractor = new SnowflakeColumnsExtractor(connection);
        } else if ("kinetica".equals(connection.getDbType())) {
            columnsExtractor = new KineticaColumnsExtractor(connection);
        } else if ("spanner".equals(connection.getDbType())) {
            columnsExtractor = new SpannerColumnsExtractor(connection);
        } else {
            columnsExtractor = new ColumnsExtractor(connection);
        }
        return new DefaultGenerator(tablesExtractor, viewExtractor, columnsExtractor, driverProvider);
    }
}
