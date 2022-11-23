package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.models.*;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.change.model.ChangeFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class DefaultChangeFinder implements ChangeFinder {

    @Override
    public List<Change<?>> findChanges(Database expected, Database actual) {
        List<Change<?>> changes = new ArrayList<>();
        if (!Objects.equals(expected.getDatabaseType(), actual.getDatabaseType())) {
            throw new RuntimeException("Can not find changes for different database types");
        }

        if (actual.getTables() == null || actual.getTables().size() == 0) {
            Change<Database> databaseChange = ChangeFactory.databaseChange(expected, null, Change.Status.ADD);
            changes.add(databaseChange);
            return changes;
        }

        // Table changes
        Collection<Table> actualTables = new ArrayList<>(actual.getTables());

        for (Table expectedTable : expected.getTables()) {
            List<Table> foundedTables = actualTables
                    .stream()
                    .filter(table -> Objects.equals(expectedTable.getName(), table.getName()) && Objects.equals(expectedTable.getSchema(), table.getSchema()))
                    .collect(Collectors.toList());

            if (foundedTables.size() == 0) {
                Change<Table> tableChange = ChangeFactory.tableChange(expectedTable, null, Change.Status.ADD);
                changes.add(tableChange);
            } else if (foundedTables.size() == 1) {
                Table table = foundedTables.get(0);
                actualTables.remove(table);
                //change in table
                List<Change<?>> changesFromTables = findChangesInColumnsForTable(expectedTable, table);
                changes.addAll(changesFromTables);
            } else {
                throw new RuntimeException(String.format("Found %d table with name '%s' and schema '%s'",
                        foundedTables.size(), expectedTable.getName(), expectedTable.getSchema()));
            }
        }

        //mark all for deletion
        for (Table actualTable : actualTables) {
            Change<Table> tableChange = ChangeFactory.tableChange(null, actualTable, Change.Status.DROP);
            changes.add(tableChange);
        }

        // Process view changes
        viewChanges(expected, actual, changes);

        log.info("Found {} changes", changes.size());
        return changes;
    }

    private void viewChanges(Database expected, Database actual, List<Change<?>> changes) {
        // Table changes
        Collection<View> actualViews = new ArrayList<>(actual.getViews());

        for (View expectedView : expected.getViews()) {
            List<View> foundViews = actualViews
                    .stream()
                    .filter(view -> Objects.equals(expectedView.getName(), view.getName()) && Objects.equals(expectedView.getSchema(), view.getSchema()))
                    .collect(Collectors.toList());

            if (foundViews.size() == 0) {
                Change<View> tableChange = ChangeFactory.viewChange(expectedView, null, Change.Status.ADD);
                changes.add(tableChange);
            } else if (foundViews.size() == 1) {
                View view = foundViews.get(0);
                actualViews.remove(view);
                //change in view - TODO -currently using table function as they identical - split in future
                List<Change<?>> changesFromView = findChangesInColumnsForTable(expectedView, view);
                changes.addAll(changesFromView);
            } else {
                throw new RuntimeException(String.format("Found %d view with name '%s' and schema '%s'",
                        foundViews.size(), expectedView.getName(), expectedView.getSchema()));
            }
        }

        //mark all for deletion
        for (View actualView : actualViews) {
            Change<View> viewChange = ChangeFactory.viewChange(null, actualView, Change.Status.DROP);
            changes.add(viewChange);
        }
    }

    private List<Change<?>> findChangesInColumnsForTable(Table expected, Table actual) {

        List<Change<?>> changes = new ArrayList<>();
        ArrayList<Column> actualColumns = new ArrayList<>(actual.getColumns());

        for (Column expectedColumn : expected.getColumns()) {
            List<Column> foundColumns = actualColumns
                    .stream()
                    .filter(column -> Objects.equals(expectedColumn.getName(), column.getName()))
                    .collect(Collectors.toList());

            if (foundColumns.isEmpty()) {
                Change<Column> columnChange = ChangeFactory.columnChange(expectedColumn, null, Change.Status.ADD, expected);
                changes.add(columnChange);
                //check if the column has fk
                List<ForeignKey> foreignKeys = expectedColumn.getForeignKeys();
                if (foreignKeys != null) {
                    foreignKeys.stream().map(fk -> ChangeFactory.foreignKeyChange(fk, null, Change.Status.ADD)).forEach(changes::add);
                }

                if (expectedColumn.isPrimaryKey()) {
                    //mark a change in the table to create PK for table
                    Change<Table> tableChange = ChangeFactory.tableChange(expected, actual, Change.Status.ALTER);
                    changes.add(tableChange);
                }

            } else if (foundColumns.size() == 1) {
                Column actualColumn = foundColumns.get(0);

                boolean same = Objects.equals(expectedColumn.isNullable(), actualColumn.isNullable())
                        && Objects.equals(expectedColumn.isAutoincrement(), actualColumn.isAutoincrement())
                        && Objects.equals(expectedColumn.isPrimaryKey(), actualColumn.isPrimaryKey())
                        && Objects.equals(expectedColumn.getTypeName(), actualColumn.getTypeName())
                        && Objects.equals(expectedColumn.getPrecision(), actualColumn.getPrecision())
                        && Objects.equals(expectedColumn.getScale(), actualColumn.getScale())
                        && Objects.equals(expectedColumn.getOrdinalPosition(), actualColumn.getOrdinalPosition())
                        && Objects.equals(expectedColumn.getColumnDisplaySize(), actualColumn.getColumnDisplaySize())
                        && Objects.equals(expectedColumn.getPrimaryKeySequenceId(), actualColumn.getPrimaryKeySequenceId());

                if (!same) {
                    Change<Column> columnChange = ChangeFactory.columnChange(expectedColumn, actualColumn, Change.Status.ALTER, expected);
                    changes.add(columnChange);
                }

                List<Change<?>> changesForForeignKeys = findChangesForForeignKeys(expectedColumn, expectedColumn.getForeignKeys(), actualColumn.getForeignKeys());
                changes.addAll(changesForForeignKeys);

                actualColumns.remove(actualColumn);

            } else {
                throw new RuntimeException(String.format("Found %d column with name '%s' in table '%s'.'%s'",
                        foundColumns.size(), actual.getName(), actual.getName(), actual.getSchema()));
            }
        }

        for (Column actualColumn : actualColumns) {
            Change<Column> columnChange = ChangeFactory.columnChange(null, actualColumn, Change.Status.DROP, expected);
            changes.add(columnChange);
        }

        return changes;
    }

    private List<Change<?>> findChangesForForeignKeys(Column column, List<ForeignKey> expectedForeignKeyList, List<ForeignKey> actualForeignKeyList) {

        List<Change<?>> changes = new ArrayList<>();
        if (expectedForeignKeyList == null && actualForeignKeyList == null) {
            return changes;
        }

        if (actualForeignKeyList == null) {
            for (ForeignKey foreignKey : expectedForeignKeyList) {
                Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(foreignKey, null, Change.Status.ADD);
                changes.add(foreignKeyChange);
            }
            return changes;
        }

        if (expectedForeignKeyList == null) {
            for (ForeignKey foreignKey : actualForeignKeyList) {
                Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(null, foreignKey, Change.Status.DROP);
                changes.add(foreignKeyChange);
            }
            return changes;
        }

        ArrayList<ForeignKey> actualForeignKeys = new ArrayList<>(actualForeignKeyList);

        for (ForeignKey foreignKey : expectedForeignKeyList) {
            List<ForeignKey> foundForeignKeys = actualForeignKeys.stream()
                    .filter(actualForeignKey -> Objects.equals(foreignKey.getName(), actualForeignKey.getName()))
                    .collect(Collectors.toList());


            if (foundForeignKeys.isEmpty()) {
                Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(foreignKey, null, Change.Status.ADD);
                changes.add(foreignKeyChange);
            } else if (foundForeignKeys.size() == 1) {
                ForeignKey actualForeignKey = foundForeignKeys.get(0);

                boolean same = Objects.equals(foreignKey.getName(), actualForeignKey.getName())
                        && Objects.equals(foreignKey.getSchema(), actualForeignKey.getSchema())
                        && Objects.equals(foreignKey.getTableName(), actualForeignKey.getTableName())
                        && Objects.equals(foreignKey.getColumnName(), actualForeignKey.getColumnName())
                        && Objects.equals(foreignKey.getDeleteRule(), actualForeignKey.getDeleteRule())
                        && Objects.equals(foreignKey.getPrimaryTableSchema(), actualForeignKey.getPrimaryTableSchema())
                        && Objects.equals(foreignKey.getPrimaryTableName(), actualForeignKey.getPrimaryTableName())
                        && Objects.equals(foreignKey.getPrimaryColumnName(), actualForeignKey.getPrimaryColumnName());

                if (!same) {
                    Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(foreignKey, actualForeignKey, Change.Status.ALTER);
                    changes.add(foreignKeyChange);
                }
                actualForeignKeys.remove(actualForeignKey);
            } else {
                throw new RuntimeException(String.format("Found %d foreign keys with name '%s' in column '%s' table '%s.%s'",
                        foundForeignKeys.size(), foreignKey.getName(), foreignKey.getColumnName(), foreignKey.getTableName(), foreignKey.getSchema()));
            }

            for (ForeignKey actualForeignKey : actualForeignKeys) {
                Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(null, actualForeignKey, Change.Status.DROP);
                changes.add(foreignKeyChange);
            }
        }

        return changes;
    }
}