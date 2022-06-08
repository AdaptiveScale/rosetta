package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;

import java.sql.Connection;
import java.util.Collection;

public class SourceGeneratorFactory {

    public static Generator<Database, Target> sourceGenerator(Target target) {
        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnExtractor<Connection, Collection<Table>> columnsExtractor = null;
        if ("bigquery".equals(target.getDbType())) {
            columnsExtractor = new BigQueryColumnsExtractor(target);
        } else {
            columnsExtractor = new ColumnsExtractor(target);
        }
        return new DefaultGenerator(tablesExtractor, columnsExtractor);
    }
}
