package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;


public interface DDL {

    String createColumn(Column column);
    String createTable(Table table, boolean dropTableIfExists);
    String createDatabase(Database database, boolean dropTableIfExists);

    String createForeignKey(ForeignKey foreignKey);


    String alterColumn(ColumnChange change);

    String dropColumn(ColumnChange change);

    String addColumn(ColumnChange change);

    String dropTable(Table actual);

    String alterForeignKey(ForeignKeyChange change);

    String dropForeignKey(ForeignKey actual);
}
