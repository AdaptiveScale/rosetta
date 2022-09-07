package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;

public class ColumnChange extends Change<Column> {
    private final Table table;

    public ColumnChange(Column expected, Column actual, Status state, Type type, Table table) {
        super(expected, actual, state, type);
        this.table = table;
    }

    public Table getTable() {
        return table;
    }
}
