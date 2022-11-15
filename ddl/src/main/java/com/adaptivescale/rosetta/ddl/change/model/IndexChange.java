package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Index;

public class IndexChange extends Change<Index> {

    public IndexChange(Index expected, Index actual, Status state, Type type) {
        super(expected, actual, state, type);
    }
}
