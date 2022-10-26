package com.adaptivescale.rosetta.ddl.targets.spanner.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;

import static com.adaptivescale.rosetta.ddl.targets.spanner.Constants.PRECISION_DEFAULTS;
import static com.adaptivescale.rosetta.ddl.targets.spanner.Constants.PRECISION_TYPES;

public class SpannerColumnTypeName implements ColumnDataTypeName {
    @Override
    public String nameForColumn(Column column) {
        StringBuilder builder = new StringBuilder();
        builder.append(ColumnDataTypeName.super.nameForColumn(column));
        if (PRECISION_TYPES.contains(column.getTypeName())) {
            String precision = PRECISION_DEFAULTS.contains(column.getPrecision())?"MAX": String.valueOf(column.getPrecision());
            builder.append("(").append(precision).append(")");
        }
        return builder.toString();
    }
}
