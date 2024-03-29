package com.adaptivescale.rosetta.ddl.targets.kinetica.decorators;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.ColumnProperties;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.adaptivescale.rosetta.ddl.targets.kinetica.Constants.PRECISION_DEFAULTS;
import static com.adaptivescale.rosetta.ddl.targets.kinetica.Constants.PRECISION_TYPES;

public class KineticaColumnTypeName implements ColumnDataTypeName {

    private final static String SHARD_KEY = "(SHARD_KEY)";

    @Override
    public String nameForColumn(Column column) {
        StringBuilder builder = new StringBuilder();
        builder.append(ColumnDataTypeName.super.nameForColumn(column));
        if ( (!PRECISION_DEFAULTS.contains(column.getPrecision()) && PRECISION_TYPES.contains(column.getTypeName().toLowerCase()))
                || (Optional.ofNullable(column.getColumnProperties()).isPresent() && !column.getColumnProperties().isEmpty())) {
            builder.append("(");
            List<String> items = new ArrayList<>();
            if (!PRECISION_DEFAULTS.contains(column.getPrecision()) && PRECISION_TYPES.contains(column.getTypeName().toLowerCase())) {
                items.add(column.getPrecision()+"");
            }

            if (Optional.ofNullable(column.getColumnProperties()).isPresent()) {
                column.getColumnProperties().forEach((columnProperty -> {
                    items.add(columnProperty.getName());
                }));
            }

            builder.append(String.join(",", items));
            builder.append(")");
        }
        //TODO: Enable this with foreign key functionality
//        if (column.getForeignKeys() != null && !column.getForeignKeys().isEmpty()) {
//            builder.append(SHARD_KEY);
//        }
        return builder.toString();
    }

}
