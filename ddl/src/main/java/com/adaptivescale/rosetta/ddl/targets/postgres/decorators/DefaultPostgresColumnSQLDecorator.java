package com.adaptivescale.rosetta.ddl.targets.postgres.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;

import static com.adaptivescale.rosetta.ddl.targets.postgres.Constants.DEFAULT_WRAPPER;

public class DefaultPostgresColumnSQLDecorator implements ColumnSQLDecorator {
    private final Column column;
    private final ColumnDataTypeName columnDataTypeName;

    public DefaultPostgresColumnSQLDecorator(Column column, ColumnDataTypeName columnDataTypeName) {
        this.column = column;
        this.columnDataTypeName = columnDataTypeName;
    }

    @Override
    public String expressSQl() {
        return DEFAULT_WRAPPER + column.getName() + DEFAULT_WRAPPER+ " "
                + columnDataTypeName.nameForColumn(column)
                + (column.isNullable() ? " NULL" : " NOT NULL");
    }
}
