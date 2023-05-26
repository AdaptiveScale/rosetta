package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.*;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.change.model.ChangeFactory;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RosettaModule(
        name = "spanner",
        type = RosettaModuleTypes.CHANGE_FINDER
)
public class SpannerChangeFinder implements ChangeFinder {
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

        Collection<Table> actualTables = new ArrayList<>(actual.getTables());
        List<ForeignKey> allForeignKeys = findAllForeignKeys(actual.getTables());

        for (Table expectedTable : expected.getTables()) {
            List<Table> foundedTables = actualTables
                    .stream()
                    .filter(table -> Objects.equals(expectedTable.getName(), table.getName()))
                    .collect(Collectors.toList());

            if (foundedTables.size() == 0) {
                Change<Table> tableSchemaChange = ChangeFactory.tableSchemaChange(expectedTable, null, Change.Status.ADD);
                changes.add(tableSchemaChange);
                Change<Table> tableChange = ChangeFactory.tableChange(expectedTable, null, Change.Status.ADD);
                changes.add(tableChange);
            } else if (foundedTables.size() == 1) {
                Table table = foundedTables.get(0);
                actualTables.remove(table);
                //change in table
                List<Change<?>> changesFromTables = findChangesInColumnsForTable(expectedTable, table, allForeignKeys);
                List<Change<?>> changesFromIndices = findChangesInIndicesForTable(expectedTable, table);
                changes.addAll(changesFromTables);
                changes.addAll(changesFromIndices);

                if (checkInterleaveChanges(table, expectedTable)) {
                    Change<Table> tableChangeDrop = ChangeFactory.tableChange(null, table, Change.Status.DROP);
                    Change<Table> tableChangeAdd = ChangeFactory.tableChange(expectedTable, null, Change.Status.ADD);
                    changes.add(tableChangeDrop);
                    changes.add(tableChangeAdd);
                }
            } else {
                throw new RuntimeException(String.format("Found %d table with name '%s' and schema '%s'",
                        foundedTables.size(), expectedTable.getName(), expectedTable.getSchema()));
            }
        }

        //mark all for deletion
        for (Table actualTable : actualTables) {

            //check if table columns are reference by any foreign keys
            List<Change<ForeignKey>> foreignKeysToDrop = dropForeignKeysThatAreReferencedToTable(actualTable, allForeignKeys);
            changes.addAll(foreignKeysToDrop);

            Change<Table> tableChange = ChangeFactory.tableChange(null, actualTable, Change.Status.DROP);
            changes.add(tableChange);
        }

        // Process view changes
        viewChanges(expected, actual, changes);

        List<Change<?>> result = filterDuplicates(changes);
        log.info("Found {} changes", result.size());
        return result;
    }

    private boolean checkInterleaveChanges(Table table, Table expectedTable) {
        if ((table.getInterleave() == null && expectedTable.getInterleave() != null) ||
                (table.getInterleave() != null && expectedTable.getInterleave() == null) ||
                (table.getInterleave() != null && expectedTable.getInterleave() != null && !table.getInterleave().equals(expectedTable.getInterleave()))) {
            return true;
        }
        return false;
    }

    private void viewChanges(Database expected, Database actual, List<Change<?>> changes) {
        // Backwards compatibility
        if (actual.getViews() == null) {
            return;
        }
        // View changes
        Collection<View> actualViews = new ArrayList<>(actual.getViews());
        List<ForeignKey> allForeignKeys = findAllForeignKeys(actual.getViews());

        for (View expectedView : expected.getViews()) {
            List<View> foundViews = actualViews
              .stream()
              .filter(view -> Objects.equals(expectedView.getName(), view.getName()) && Objects.equals(expectedView.getSchema(), view.getSchema()))
              .collect(Collectors.toList());

            if (foundViews.size() == 0) {
                Change<View> viewChange = ChangeFactory.viewChange(expectedView, null, Change.Status.ADD);
                changes.add(viewChange);
            } else if (foundViews.size() == 1) {
                View view = foundViews.get(0);
                actualViews.remove(view);
                //change in view - TODO -currently using table function as they identical - split in future
                List<Change<?>> changesFromView = findChangesInColumnsForTable(expectedView, view, allForeignKeys);
                if(!changesFromView.isEmpty()) {
                    Change<View> viewChange = ChangeFactory.viewChange(expectedView, null, Change.Status.ALTER);
                    changes.add(viewChange);
                }
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

    private List<Change<?>> findChangesInIndicesForTable(Table expected, Table actual) {
        List<Change<?>> changes = new ArrayList<>();
        ArrayList<Index> actualIndices = new ArrayList<>(actual.getIndices());

        for (Index expectedIndex : expected.getIndices()) {
            List<Index> foundIndices = actualIndices
                .stream()
                .filter(index -> Objects.equals(expectedIndex.getName(), index.getName()))
                .collect(Collectors.toList());

            if (foundIndices.isEmpty()) {
                Change<Index> indexChange = ChangeFactory.indexChange(expectedIndex, null, Change.Status.ADD);
                changes.add(indexChange);

            } else if (foundIndices.size() == 1) {
                Index actualIndex = foundIndices.get(0);

                boolean same = Objects.equals(expectedIndex, actualIndex);

                if (!same) {
                    changes.add(ChangeFactory.indexChange(expectedIndex, actualIndex, Change.Status.DROP));
                    changes.add(ChangeFactory.indexChange(expectedIndex, actualIndex, Change.Status.ADD));
                }

                for (Index foundIndex: foundIndices) {
                    actualIndices.remove(foundIndex);
                }
            } else {
                throw new RuntimeException(String.format("Found %d indices with name '%s' in table '%s'.'%s'",
                        foundIndices.size(), actual.getName(), actual.getName(), actual.getSchema()));
            }
        }

        for (Index actualIndex : actualIndices) {
            Change<Index> indexChange = ChangeFactory.indexChange(null, actualIndex, Change.Status.DROP);
            changes.add(indexChange);
        }
        return changes;
    }

    private String extractFromSpannerJdbcTypeName(Column column) {
        String compatibleType = column.getTypeName();
        if(column.getTypeName().contains("(")){
             compatibleType = column.getTypeName().substring(0, column.getTypeName().indexOf("("));
        }
        return compatibleType;
    }

    private List<Change<?>> findChangesInColumnsForTable(Table expected, Table actual, List<ForeignKey> allForeignKeys) {

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
                        && Objects.equals(expectedColumn.getTypeName(), extractFromSpannerJdbcTypeName(actualColumn))
                        && Objects.equals(expectedColumn.getPrecision(), actualColumn.getPrecision())
                        && Objects.equals(expectedColumn.getScale(), actualColumn.getScale())
                        && Objects.equals(expectedColumn.getOrdinalPosition(), actualColumn.getOrdinalPosition())
                        && Objects.equals(expectedColumn.getColumnDisplaySize(), actualColumn.getColumnDisplaySize())
                        && Objects.equals(expectedColumn.getPrimaryKeySequenceId(), actualColumn.getPrimaryKeySequenceId());

                if (!same) {
                    Change<Column> columnChange = ChangeFactory.columnChange(expectedColumn, actualColumn, Change.Status.ALTER, expected);
                    changes.add(columnChange);
                }

                // changes for primary keep handle in table bcs there can be multiple column involved
                if (!Objects.equals(expectedColumn.isPrimaryKey(), actualColumn.isPrimaryKey())) {

                    if (actualColumn.isPrimaryKey()) {
                        List<Change<ForeignKey>> dropForeignKeys = dropForeignKeysThatAreReferencedToColumn(actualColumn, actual, allForeignKeys);
                        changes.addAll(dropForeignKeys);
                    }

                    Change<Table> tableChange = ChangeFactory.tableChange(expected, actual, Change.Status.ALTER);
                    changes.add(tableChange);
                }


                List<Change<?>> changesForForeignKeys = findChangesForForeignKeys(expectedColumn.getForeignKeys(), actualColumn.getForeignKeys());
                changes.addAll(changesForForeignKeys);

                actualColumns.remove(actualColumn);

            } else {
                throw new RuntimeException(String.format("Found %d column with name '%s' in table '%s'.'%s'",
                        foundColumns.size(), actual.getName(), actual.getName(), actual.getSchema()));
            }
        }


        for (Column actualColumn : actualColumns) {

            if (actualColumn.getForeignKeys() != null) {
                List<Change<ForeignKey>> collect = actualColumn.getForeignKeys().stream().map(foreignKey -> ChangeFactory.foreignKeyChange(null, foreignKey, Change.Status.DROP)).collect(Collectors.toList());
                changes.addAll(collect);
            }


            List<Change<ForeignKey>> foreignKeyToDrop = dropForeignKeysThatAreReferencedToColumn(actualColumn, actual, allForeignKeys);
            changes.addAll(foreignKeyToDrop);


            Change<Column> columnChange = ChangeFactory.columnChange(null, actualColumn, Change.Status.DROP, expected);
            changes.add(columnChange);
        }


        return changes;
    }

    private List<Change<?>> findChangesForForeignKeys(List<ForeignKey> expectedForeignKeyList, List<ForeignKey> actualForeignKeyList) {

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
                    changes.add(ChangeFactory.foreignKeyChange(foreignKey, actualForeignKey, Change.Status.DROP));
                    changes.add(ChangeFactory.foreignKeyChange(foreignKey, actualForeignKey, Change.Status.ADD));
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


    private List<Change<?>> filterDuplicates(List<Change<?>> changes) {
        Set<String> foreignKeysFound = new HashSet<>();

        return changes.stream().filter(change -> {

            String id = null;
            Object object = change.getActual() != null ? change.getActual() : change.getExpected();
            if (object instanceof ForeignKey) {
                id = "FOREIGN_KEY->" + change.getStatus() + "->" + ((ForeignKey) object).getSchema() + "->" +
                        ((ForeignKey) object).getTableName() + "->" + ((ForeignKey) object).getColumnName() + "->" +
                        ((ForeignKey) object).getName();
            }

            if (object instanceof Column) {
                Table table = ((ColumnChange) change).getTable();
                id = "COLUMN->" + change.getStatus() + "->" + table.getSchema() + "->" + table.getName() + "->" + ((Column) object).getName();
            }

            if (object instanceof Table) {
                id = "TABLE->" + change.getStatus() + "->" + ((Table) object).getSchema() + "->" + ((Table) object).getName();
            }

            if (object instanceof Database) {
                id = "DATABASE->" + change.getStatus() + "->" + ((Database) object).getDatabaseType();
            }

            if (object instanceof Index) {
                id = "INDEX->" + change.getStatus() + "->" + ((Index) object).getSchema() + "->" + ((Index) object).getTableName() + "->" + ((Index) object).getName();
            }

            boolean contains = foreignKeysFound.contains(id);
            if (contains) {
                return false;
            }

            foreignKeysFound.add(id);
            return true;
        }).collect(Collectors.toList());
    }

    private List<Change<ForeignKey>> dropForeignKeysThatAreReferencedToTable(Table tableToDrop, List<ForeignKey> allForeignKeys) {

        List<Change<ForeignKey>> foreignKeysToDrop = new ArrayList<>();
        for (Column column : tableToDrop.getColumns()) {
            List<Change<ForeignKey>> changes = dropForeignKeysThatAreReferencedToColumn(column, tableToDrop, allForeignKeys);
            foreignKeysToDrop.addAll(changes);
        }

        return foreignKeysToDrop;
    }

    private List<Change<ForeignKey>> dropForeignKeysThatAreReferencedToColumn(Column columnToDrop, Table columnTable, List<ForeignKey> allForeignKeys) {

        List<Change<ForeignKey>> foreignKeysToDrop = new ArrayList<>();
        for (ForeignKey foreignKey : allForeignKeys) {
            if (Objects.equals(foreignKey.getPrimaryTableSchema(), columnTable.getSchema())
                    && Objects.equals(foreignKey.getPrimaryTableName(), columnTable.getName())
                    && Objects.equals(foreignKey.getPrimaryColumnName(), columnToDrop.getName())
            ) {
                Change<ForeignKey> foreignKeyChange = ChangeFactory.foreignKeyChange(null, foreignKey, Change.Status.DROP);
                foreignKeysToDrop.add(foreignKeyChange);
            }
        }

        return foreignKeysToDrop;
    }

    private List<ForeignKey> findAllForeignKeys(Collection<? extends Table> tables) {
        return tables.stream().flatMap((Function<Table, Stream<ForeignKey>>) table
                        -> table.getColumns().stream().flatMap((Function<Column, Stream<ForeignKey>>) column -> column.getForeignKeys() == null ? Stream.empty() : column.getForeignKeys().stream()))
                .collect(Collectors.toList());
    }
}
