package com.adaptivescale.rosetta.ddl.targets.snowflake.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;

public class DefaultSnowflakeColumnSQLDecorator implements ColumnSQLDecorator {
    private final Column column;
    private final ColumnDataTypeName columnDataTypeName;

    public DefaultSnowflakeColumnSQLDecorator(Column column, ColumnDataTypeName columnDataTypeName) {

        this.column = column;
        this.columnDataTypeName = columnDataTypeName;
    }

    @Override
    public String expressSQl() {
        return "\"" + column.getName() + "\" "
                + columnDataTypeName.nameForColumn(column)
                + (column.isNullable() ? "" : " not null")
                + (column.isAutoincrement() ? " AUTOINCREMENT" : "");
    }
}
