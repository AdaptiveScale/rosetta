package com.adaptivescale.rosetta.ddl.targets.redshift.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;

import static com.adaptivescale.rosetta.ddl.targets.redshift.Constants.DEFAULT_WRAPPER;

public class DefaultRedshiftColumnSQLDecorator implements ColumnSQLDecorator {

    private final Column column;
    private final ColumnDataTypeName columnDataTypeName;

    public DefaultRedshiftColumnSQLDecorator(Column column, ColumnDataTypeName columnDataTypeName) {
        this.column = column;
        this.columnDataTypeName = columnDataTypeName;
    }

    @Override
    public String expressSQl() {
        StringBuilder builder = new StringBuilder();
        builder.append(DEFAULT_WRAPPER).append(column.getName()).append(DEFAULT_WRAPPER).append(" ");
        builder.append(columnDataTypeName.nameForColumn(column));
        if(column.isAutoincrement()){
            builder.append(" IDENTITY(1, 1) ");
        }
        if(!column.isNullable()) {
            builder.append(" NOT NULL ");
        }

        return builder.toString();
    }

}
