package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.*;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;


public interface DDLExtensionTable {

    String preCreateTable(Table table,  Object action);
    String postCreateTable(Table table,  Object action);

    String preDropTable(Table actual, Object action);
    String postDropTable(Table actual, Object action);

    String preAlterTable(Table expected, Object action);
    String postAlterTable(Table expected, Object action);

}
