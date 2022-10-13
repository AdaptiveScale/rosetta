package com.adaptivescale.rosetta.ddl.targets.postgres;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.postgres.decorators.DefaultPostgresColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.postgres.decorators.PostgresColumnTypeName;

public class PostgresColumnDecoratorFactory implements ColumnSQLDecoratorFactory {
    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new PostgresColumnTypeName();
        return new DefaultPostgresColumnSQLDecorator(column, columnDataTypeName);
    }
}
