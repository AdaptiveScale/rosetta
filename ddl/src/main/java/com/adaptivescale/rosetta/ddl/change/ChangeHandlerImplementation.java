package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.DatabaseChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChangeHandlerImplementation implements ChangeHandler{

    private final DDL ddl;
    private final Comparator<Change<?>> changeComparator;

    public ChangeHandlerImplementation(DDL ddl, Comparator<Change<?>> changeComparator) {
        this.ddl = ddl;
        this.changeComparator = changeComparator;
    }

    @Override
    public String createDDLForChanges(Database expected, Database actual) {
        List<Change<?>> changes = new ChangeFinder().findChanges(expected, actual);

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
}
