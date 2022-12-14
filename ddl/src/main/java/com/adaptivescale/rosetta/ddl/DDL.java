package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.*;
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

    String alterTable(Table expected, Table actual);

    default String createIndex(Index index) {
        return null;
    }

    default String dropIndex(Index actual) {
        return null;
    }

    default String dropView(View actual) {
        return "";
    };

    default String createView(View expected, boolean dropViewIfExists) {
        return "";
    };

    default String alterView(View expected, View actual) {
        return "";
    };
}
