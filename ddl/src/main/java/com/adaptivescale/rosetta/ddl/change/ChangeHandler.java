package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.ddl.change.model.*;

import java.util.List;

public interface ChangeHandler {

    String createDDLForChanges(List<Change<?>> changes);

    String onDatabaseChange(DatabaseChange databaseChange);

    String onTableChange(TableChange change);

    String onColumnChange(ColumnChange change);

    String onForeignKeyChange(ForeignKeyChange change);
}
