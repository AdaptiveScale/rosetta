package com.adaptivescale.rosetta.ddl.targets.snowflake.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;

public class SnowflakeNumericColumnSQLDecorator implements ColumnDataTypeName {

    @Override
    public String nameForColumn(Column column) {
        return column.getTypeName()+ "(" + column.getPrecision() + " ," + column.getScale() + ")";
    }
}
