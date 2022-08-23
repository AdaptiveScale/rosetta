package com.adaptivescale.rosetta.ddl.change;


import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.change.model.Change;

public class TableChange extends Change<Table> {
    public TableChange(Table expected, Table actual, Status state, Type type) {
        super(expected, actual, state, type);
    }
}
