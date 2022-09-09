package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.List;

public interface ChangeFinder {
    List<Change<?>> findChanges(Database expected, Database actual);
}
