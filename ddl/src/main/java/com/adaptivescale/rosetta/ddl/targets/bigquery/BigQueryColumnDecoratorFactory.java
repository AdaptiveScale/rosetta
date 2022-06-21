package com.adaptivescale.rosetta.ddl.targets.bigquery;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.bigquery.decorators.DefaultBigQueryColumnSQLDecorator;

public class BigQueryColumnDecoratorFactory implements ColumnSQLDecoratorFactory {
    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new ColumnDataTypeName() {};
        return new DefaultBigQueryColumnSQLDecorator(column, columnDataTypeName);
    }
}
