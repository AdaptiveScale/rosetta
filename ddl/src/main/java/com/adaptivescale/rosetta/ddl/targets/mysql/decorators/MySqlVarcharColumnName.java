package com.adaptivescale.rosetta.ddl.targets.mysql.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;

public class MySqlVarcharColumnName implements ColumnDataTypeName {
    private final int MAX_LENGTH = 65535;

    @Override
    public String nameForColumn(Column column) {
        int maxLength = Math.min(column.getColumnDisplaySize(), MAX_LENGTH);
        return column.getTypeName()
                + "(" + maxLength + ")";
    }
}
