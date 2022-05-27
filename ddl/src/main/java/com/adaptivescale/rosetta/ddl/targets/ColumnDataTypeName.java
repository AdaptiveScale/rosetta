package com.adaptivescale.rosetta.ddl.targets;

import com.adaptivescale.rosetta.common.models.Column;

public interface ColumnDataTypeName {
    default String nameForColumn(Column column){
        return column.getTypeName();
    }
}
