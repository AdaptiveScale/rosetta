package com.adaptivescale.rosetta.ddl.change.model;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;


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
}
