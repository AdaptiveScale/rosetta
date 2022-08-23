package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.DatabaseChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;

public interface ChangeHandler {

    String createDDLForChanges(Database expected, Database actual);

    String onDatabaseChange(DatabaseChange databaseChange);

    String onTableChange(TableChange change);

    String onColumnChange(ColumnChange change);

    String onForeignKeyChange(ForeignKeyChange change);
}
