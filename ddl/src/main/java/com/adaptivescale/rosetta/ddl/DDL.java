package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;

public interface DDL {

    String createColumn(Column column);
    String createTable(Table table);
    String createDataBase(Database database);
}
