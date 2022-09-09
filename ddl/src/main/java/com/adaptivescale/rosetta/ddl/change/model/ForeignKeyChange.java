package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.ForeignKey;

public class ForeignKeyChange extends Change<ForeignKey> {

    public ForeignKeyChange(ForeignKey expected, ForeignKey actual, Status state, Type type) {
        super(expected, actual, state, type);
    }

}
