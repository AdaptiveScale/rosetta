package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Table;

public class TableSchemaChange extends Change<Table> {

    public TableSchemaChange(Table expected, Table actual, Status state, Type type) {
        super(expected, actual, state, type);
    }
}
