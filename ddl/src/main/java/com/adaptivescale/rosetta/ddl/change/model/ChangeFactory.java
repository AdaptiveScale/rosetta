package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.*;


public class ChangeFactory {

    public static Change<Column> columnChange(Column expected, Column actual, Change.Status status, Table table){
        return new ColumnChange(expected,actual, status, Change.Type.COLUMN,table);
    }

    public static Change<Table> tableChange(Table expected, Table actual, Change.Status status){
        return new TableChange(expected, actual, status, Change.Type.TABLE);
    }

    public static Change<Database> databaseChange(Database expected, Database actual, Change.Status status){
        return new DatabaseChange(expected, actual, status, Change.Type.DATABASE);
    }

    public static Change<ForeignKey> foreignKeyChange(ForeignKey expected, ForeignKey actual, Change.Status status){
        return new ForeignKeyChange(expected, actual, status, Change.Type.FOREIGN_KEY);
    }

    public static Change<Index> indexChange(Index expected, Index actual, Change.Status status) {
        return new IndexChange(expected, actual, status, Change.Type.INDEX);
    }

    public static Change<View> viewChange(View expected, View actual, Change.Status status){
        return new ViewChange(expected, actual, status, Change.Type.VIEW);
    }
}
