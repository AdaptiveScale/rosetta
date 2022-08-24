package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Table;

public class TableChange extends Change<Table> {
    public TableChange(Table expected, Table actual, Status state, Type type) {
        super(expected, actual, state, type);
    }
}
