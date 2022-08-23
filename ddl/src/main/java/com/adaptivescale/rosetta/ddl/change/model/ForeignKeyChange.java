package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.ForeignKey;

public class ForeignKeyChange extends Change<ForeignKey> {
    private final Column column;

    public ForeignKeyChange(ForeignKey expected, ForeignKey actual, Status state, Type type, Column columnChange) {
        super(expected, actual, state, type);
        this.column = columnChange;
    }

    public Column getColumn() {
        return column;
    }
}
