package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ChangeHandlerImplementation implements ChangeHandler{

    private final DDL ddl;
    private final Comparator<Change<?>> changeComparator;

    public ChangeHandlerImplementation(DDL ddl, Comparator<Change<?>> changeComparator) {
        this.ddl = ddl;
        this.changeComparator = changeComparator;
    }

    @Override
    public String createDDLForChanges(List<Change<?>> changes) {
        if(changeComparator != null){
            changes.sort(changeComparator);
        }

        List<String> ddlStatements = new ArrayList<>();
        for (Change<?> change : changes) {
            switch (change.getType()) {
                case DATABASE:
                    ddlStatements.add(onDatabaseChange((DatabaseChange) change));
                    break;
                case TABLE:
                    ddlStatements.add(onTableChange((TableChange) change));
                    break;
                case COLUMN:
                    ddlStatements.add(onColumnChange((ColumnChange) change));
                    break;
                case FOREIGN_KEY:
                    ddlStatements.add(onForeignKeyChange((ForeignKeyChange) change));
                    break;
                case INDEX:
                    ddlStatements.add(onIndexChange((IndexChange) change));
                case VIEW:
                    ddlStatements.add(onViewChange((ViewChange) change));
            }
        }

        return String.join("\r", ddlStatements);
    }

    @Override
    public String onDatabaseChange(DatabaseChange databaseChange) {
        switch (databaseChange.getStatus()) {
            case ADD:
                return ddl.createDatabase(databaseChange.getExpected(), false);
            case ALTER:
            case DROP:
            default:
                throw new RuntimeException("Operation " + databaseChange.getStatus() + " for database not supported");
        }
    }

    @Override
    public String onTableChange(TableChange change) {
        switch (change.getStatus()) {
            case DROP:
                return ddl.dropTable(change.getActual());
            case ADD:
                return ddl.createTable(change.getExpected(), false);
            case ALTER:
                return ddl.alterTable(change.getExpected(), change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for table not supported");
        }
    }

    @Override
    public String onColumnChange(ColumnChange change) {
        switch (change.getStatus()) {
            case ALTER:
                return ddl.alterColumn(change);
            case DROP:
                return ddl.dropColumn(change);
            case ADD:
                return ddl.addColumn(change);
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for column not supported");
        }
    }

    @Override
    public String onForeignKeyChange(ForeignKeyChange change) {
        switch (change.getStatus()) {
            case ADD:
                return ddl.createForeignKey(change.getExpected());
            case ALTER:
                return ddl.alterForeignKey(change);
            case DROP:
                return ddl.dropForeignKey(change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for foreign key not supported");
        }
    }

    @Override
    public String onIndexChange(IndexChange change) {
        switch (change.getStatus()) {
            case ADD:
                return ddl.createIndex(change.getExpected());
            case DROP:
                return ddl.dropIndex(change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for index not supported");
        }
    }

    @Override
    public String onViewChange(ViewChange change) {
        switch (change.getStatus()) {
            case DROP:
                return ddl.dropView(change.getActual());
            case ADD:
                return ddl.createView(change.getExpected(), false);
            case ALTER:
                return ddl.alterView(change.getExpected(), change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for view not supported");
        }
    }
}
