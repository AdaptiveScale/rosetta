package com.adaptivescale.rosetta.ddl.targets.kinetica.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.ColumnProperties;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;

import java.util.List;

import static com.adaptivescale.rosetta.ddl.targets.postgres.Constants.DEFAULT_WRAPPER;

public class DefaultKineticaColumnSQLDecorator implements ColumnSQLDecorator {
    private final Column column;
    private final ColumnDataTypeName columnDataTypeName;

    public DefaultKineticaColumnSQLDecorator(Column column, ColumnDataTypeName columnDataTypeName) {
        this.column = column;
        this.columnDataTypeName = columnDataTypeName;
    }

    @Override
    public String expressSQl() {
        StringBuilder builder = new StringBuilder();
        builder.append(DEFAULT_WRAPPER).append(column.getName()).append(DEFAULT_WRAPPER).append(" ");
        builder.append(columnDataTypeName.nameForColumn(column));
        if(!column.isNullable()) {
            builder.append(" NOT NULL ");
        }
        return builder.toString();
    }

}
