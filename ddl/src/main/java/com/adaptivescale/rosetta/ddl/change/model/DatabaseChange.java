package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Database;

public class DatabaseChange extends Change<Database> {

    public DatabaseChange(Database expected, Database actual, Status state, Type type) {
        super(expected, actual, state, type);
    }
}
