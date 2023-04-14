package com.adaptivescale.rosetta.ddl.targets.db2.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;

import static com.adaptivescale.rosetta.ddl.targets.db2.Constants.DEFAULT_WRAPPER;

public class DefaultDB2ColumnSQLDecorator implements ColumnSQLDecorator {

    private final Column column;
    private final ColumnDataTypeName columnDataTypeName;


    public DefaultDB2ColumnSQLDecorator(Column column, ColumnDataTypeName columnDataTypeName) {
        this.column = column;
        this.columnDataTypeName = columnDataTypeName;
    }

    @Override
    public String expressSQl() {
        StringBuilder builder = new StringBuilder();
        builder.append(DEFAULT_WRAPPER).append(column.getName()).append(DEFAULT_WRAPPER).append(" ");
        builder.append(columnDataTypeName.nameForColumn(column));
        if(!column.isNullable()) {
            builder.append(" NOT NULL WITH DEFAULT ");
        }
        return builder.toString();
    }

}
