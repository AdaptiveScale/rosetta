package com.adaptivescale.rosetta.diff;

import com.adaptivescale.rosetta.common.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultTester implements Diff<List<String>, Database, Database> {

    private static final String TABLE_COLUMNS_CHANGED_FORMAT = "Table Changed: Table '%s' columns changed";
    private static final String TABLE_REMOVED_FORMAT = "Table Removed: Table '%s'";
    private static final String TABLE_ADDED_FORMAT = "Table Added: Table '%s'";

    private static final String COLUMN_CHANGED_FORMAT = "Column Changed: Column '%s' in table '%s' changed '%s'. Old value: '%s', new value: '%s'";
    private static final String COLUMN_REMOVED_FORMAT = "Column Removed: Column '%s' in table '%s'";
    private static final String COLUMN_ADDED_FORMAT = "Column Added: Column '%s' in table '%s'";

    private static final String COLUMN_FOREIGN_KEY_CHANGED = "Foreign Key Changed: FK '%s' on Column '%s' in table '%s' changed '%s'. Old value: '%s', new value: '%s'";
    private static final String COLUMN_FOREIGN_KEY_ADDED = "Foreign Key Added: FK '%s'  on Column '%s' in table '%s' is added";
    private static final String COLUMN_FOREIGN_KEY_REMOVED = "Foreign Key Changed: FK '%s' on Column '%s' in table '%s' is removed";

    private static final String INDEX_CHANGED_FORMAT = "Index Changed: Index '%s'";
    private static final String INDEX_REMOVED_FORMAT = "Index Removed: Index '%s'";
    private static final String INDEX_ADDED_FORMAT = "Index Added: Index '%s'";

    private static final String VIEW_REMOVED_FORMAT = "View Removed: Table '%s'";

    private static final String VIEW_COLUMNS_CHANGED_FORMAT = "View Changed: View '%s' columns changed";
    private static final String VIEW_ADDED_FORMAT = "View Added: View '%s'";

    @Override
    public List<String> find(Database localValue, Database targetValue) {

        List<String> changes = new ArrayList<>();

        //do we need to check for root properties if are changed
        for (Table table : localValue.getTables()) {
            List<String> columnsChangesLogs = new ArrayList<>();
            Optional<Table> targetTable = getTable(table.getName(), targetValue);
            if (targetTable.isEmpty()) {
                //this table is removed
                changes.add(String.format(TABLE_REMOVED_FORMAT, table.getName()));
                continue;
            }

            Collection<Column> columns = table.getColumns();
            for (Column localColumn : columns) {
                Optional<Column> targetColumn = getColumn(localColumn.getName(), targetTable.get());
                if (targetColumn.isEmpty()) {
                    //this column was removed
                    columnsChangesLogs.add(String.format(COLUMN_REMOVED_FORMAT, localColumn.getName(), table.getName()));
                    continue;
                }

                if (!Objects.equals(localColumn.getDescription(), targetColumn.get().getDescription())) {
                    String description = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Description", localColumn.getDescription(),
                            targetColumn.get().getDescription());
                    columnsChangesLogs.add(description);
                }

                if (!Objects.equals(localColumn.getColumnDisplaySize(), targetColumn.get().getColumnDisplaySize())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Display Size", localColumn.getColumnDisplaySize(),
                            targetColumn.get().getColumnDisplaySize());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getLabel(), targetColumn.get().getLabel())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Label", localColumn.getLabel(),
                            targetColumn.get().getLabel());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getOrdinalPosition(), targetColumn.get().getOrdinalPosition())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Ordinal Position", localColumn.getOrdinalPosition(),
                            targetColumn.get().getOrdinalPosition());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getPrecision(), targetColumn.get().getPrecision())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Precision", localColumn.getPrecision(),
                            targetColumn.get().getPrecision());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getTypeName(), targetColumn.get().getTypeName())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Type Name", localColumn.getTypeName(),
                            targetColumn.get().getTypeName());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isAutoincrement(), targetColumn.get().isAutoincrement())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Autoincrement", localColumn.isAutoincrement(),
                            targetColumn.get().isAutoincrement());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isPrimaryKey(), targetColumn.get().isPrimaryKey())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Primary key", localColumn.isPrimaryKey(),
                            targetColumn.get().isPrimaryKey());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isNullable(), targetColumn.get().isNullable())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Nullable", localColumn.isNullable(),
                            targetColumn.get().isNullable());
                    columnsChangesLogs.add(result);
                }

                columnsChangesLogs.addAll(sameForeignKeys(localColumn.getForeignKeys(), targetColumn.get().getForeignKeys()));
            }

            //check what columns are added, by filtering what is not in local model
            Set<String> localColumnsName = columns.stream().map(Column::getName).collect(Collectors.toSet());
            List<Column> addedColumns = targetTable
                    .get()
                    .getColumns()
                    .stream()
                    .filter(column -> !localColumnsName.contains(column.getName()))
                    .collect(Collectors.toList());

            addedColumns.forEach(column -> columnsChangesLogs.add(String.format(COLUMN_ADDED_FORMAT, column.getName(), table.getName())));
            if (columnsChangesLogs.size() > 0) {
                changes.add(String.format(TABLE_COLUMNS_CHANGED_FORMAT, table.getName()));
                changes.addAll(columnsChangesLogs);
            }

            changes.addAll(sameIndices(table.getIndices(), targetTable.get().getIndices()));
        }

        Set<String> localTablesName = localValue.getTables().stream().map(Table::getName).collect(Collectors.toSet());
        List<Table> tablesAdded = targetValue
                .getTables()
                .stream()
                .filter(table -> !localTablesName.contains(table.getName()))
                .collect(Collectors.toList());
        tablesAdded.forEach(table -> changes.add(String.format(TABLE_ADDED_FORMAT, table.getName())));

        // Check views for changes
        testViews(localValue, targetValue, changes);
        return changes;
    }

    private void testViews(Database localValue, Database targetValue, List<String> changes) {
        //do we need to check for root properties if are changed
        for (View view : localValue.getViews()) {
            List<String> columnsChangesLogs = new ArrayList<>();
            Optional<View> targetView = getView(view.getName(), targetValue);
            if (targetView.isEmpty()) {
                //this table is removed
                changes.add(String.format(VIEW_REMOVED_FORMAT, view.getName()));
                continue;
            }

            Collection<Column> columns = view.getColumns();
            for (Column localColumn : columns) {
                Optional<Column> targetColumn = getColumn(localColumn.getName(), targetView.get());
                if (targetColumn.isEmpty()) {
                    //this column was removed
                    columnsChangesLogs.add(String.format(COLUMN_REMOVED_FORMAT, localColumn.getName(), view.getName()));
                    continue;
                }

                if (!Objects.equals(localColumn.getDescription(), targetColumn.get().getDescription())) {
                    String description = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Description", localColumn.getDescription(),
                            targetColumn.get().getDescription());
                    columnsChangesLogs.add(description);
                }

                if (!Objects.equals(localColumn.getColumnDisplaySize(), targetColumn.get().getColumnDisplaySize())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Display Size", localColumn.getColumnDisplaySize(),
                            targetColumn.get().getColumnDisplaySize());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getLabel(), targetColumn.get().getLabel())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Label", localColumn.getLabel(),
                            targetColumn.get().getLabel());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getOrdinalPosition(), targetColumn.get().getOrdinalPosition())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Ordinal Position", localColumn.getOrdinalPosition(),
                            targetColumn.get().getOrdinalPosition());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getPrecision(), targetColumn.get().getPrecision())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Precision", localColumn.getPrecision(),
                            targetColumn.get().getPrecision());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.getTypeName(), targetColumn.get().getTypeName())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Type Name", localColumn.getTypeName(),
                            targetColumn.get().getTypeName());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isAutoincrement(), targetColumn.get().isAutoincrement())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Autoincrement", localColumn.isAutoincrement(),
                            targetColumn.get().isAutoincrement());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isPrimaryKey(), targetColumn.get().isPrimaryKey())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Primary key", localColumn.isPrimaryKey(),
                            targetColumn.get().isPrimaryKey());
                    columnsChangesLogs.add(result);
                }

                if (!Objects.equals(localColumn.isNullable(), targetColumn.get().isNullable())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            view.getName(), "Nullable", localColumn.isNullable(),
                            targetColumn.get().isNullable());
                    columnsChangesLogs.add(result);
                }

                columnsChangesLogs.addAll(sameForeignKeys(localColumn.getForeignKeys(), targetColumn.get().getForeignKeys()));
            }

            //check what columns are added, by filtering what is not in local model
            Set<String> localColumnsName = columns.stream().map(Column::getName).collect(Collectors.toSet());
            List<Column> addedColumns = targetView
                    .get()
                    .getColumns()
                    .stream()
                    .filter(column -> !localColumnsName.contains(column.getName()))
                    .collect(Collectors.toList());

            addedColumns.forEach(column -> columnsChangesLogs.add(String.format(COLUMN_ADDED_FORMAT, column.getName(), view.getName())));
            if (columnsChangesLogs.size() > 0) {
                changes.add(String.format(VIEW_COLUMNS_CHANGED_FORMAT, view.getName()));
                changes.addAll(columnsChangesLogs);
            }

            changes.addAll(sameIndices(view.getIndices(), targetView.get().getIndices()));
        }
        Set<String> localViewName = localValue.getViews().stream().map(View::getName).collect(Collectors.toSet());
        List<View> viewsAdded = targetValue
                .getViews()
                .stream()
                .filter(table -> !localViewName.contains(table.getName()))
                .collect(Collectors.toList());
        viewsAdded.forEach(view -> changes.add(String.format(VIEW_ADDED_FORMAT, view.getName())));
    }



    private List<String> sameIndices(List<Index> localIndices, List<Index> targetIndices) {
        List<String> changeLogs = new ArrayList<>();

        if (localIndices == targetIndices) {
            return changeLogs;
        }
        if (localIndices == null) {
            localIndices = new ArrayList<>();
        }

        if (targetIndices == null) {
            targetIndices = new ArrayList<>();
        }

        for (Index localIndex : localIndices) {
            Optional<Index> targetIndex = getIndexByName(targetIndices, localIndex);
            if (targetIndex.isEmpty()) {
                // foreign key is removed
                String change = String.format(INDEX_REMOVED_FORMAT,
                    localIndex.getName(),
                    localIndex.getColumnNames(),
                    localIndex.getTableName());
                changeLogs.add(change);
                continue;
            }

            if (!Objects.equals(localIndex, targetIndex.get())) {
                String change = String.format(INDEX_CHANGED_FORMAT,
                        localIndex.getName(),
                        localIndex.getColumnNames(),
                        localIndex.getTableName());

                changeLogs.add(change);
            }
        }

        Set<String> localIndicesNames = localIndices.stream().map(Index::getName).collect(Collectors.toSet());
        targetIndices
            .stream()
            .filter(index -> !localIndicesNames.contains(index.getName()))
            .forEach(index -> {
                String change = String.format(INDEX_ADDED_FORMAT,
                        index.getName(),
                        index.getColumnNames(),
                        index.getTableName());
                changeLogs.add(change);
            });

        return changeLogs;
    }

    private List<String> sameForeignKeys(List<ForeignKey> localForeignKeys, List<ForeignKey> targetForeignKeys) {

        List<String> changeLogs = new ArrayList<>();

        if (localForeignKeys == targetForeignKeys) {
            return changeLogs;
        }
        if (localForeignKeys == null) {
            localForeignKeys = new ArrayList<>();
        }

        if (targetForeignKeys == null) {
            targetForeignKeys = new ArrayList<>();
        }

        for (ForeignKey localForeignKey : localForeignKeys) {
            Optional<ForeignKey> targetForeignKey = getForeignKeyByName(targetForeignKeys, localForeignKey);
            if (targetForeignKey.isEmpty()) {
                // foreign key is removed
                String change = String.format(COLUMN_FOREIGN_KEY_REMOVED,
                        localForeignKey.getName(),
                        localForeignKey.getColumnName(),
                        localForeignKey.getTableName());
                changeLogs.add(change);
                continue;
            }

            if (!Objects.equals(localForeignKey.getDeleteRule(), targetForeignKey.get().getDeleteRule())) {
                String change = String.format(COLUMN_FOREIGN_KEY_CHANGED,
                        localForeignKey.getName(),
                        localForeignKey.getColumnName(),
                        localForeignKey.getTableName(),
                        "Delete Rule",
                        localForeignKey.getDeleteRule(),
                        targetForeignKey.get().getDeleteRule());

                changeLogs.add(change);
            }

            if (!Objects.equals(localForeignKey.getPrimaryColumnName(), targetForeignKey.get().getPrimaryColumnName())) {
                String change = String.format(COLUMN_FOREIGN_KEY_CHANGED,
                        localForeignKey.getName(),
                        localForeignKey.getColumnName(),
                        localForeignKey.getTableName(),
                        "Primary Key Column Name",
                        localForeignKey.getPrimaryColumnName(),
                        targetForeignKey.get().getPrimaryColumnName());

                changeLogs.add(change);
            }

            if (!Objects.equals(localForeignKey.getPrimaryTableName(), targetForeignKey.get().getPrimaryTableName())) {
                String change = String.format(COLUMN_FOREIGN_KEY_CHANGED,
                        localForeignKey.getName(),
                        localForeignKey.getColumnName(),
                        localForeignKey.getTableName(),
                        "Primary Key Table Name", localForeignKey.getPrimaryTableName(),
                        targetForeignKey.get().getPrimaryTableName());

                changeLogs.add(change);
            }

            if (!Objects.equals(localForeignKey.getPrimaryTableSchema(), targetForeignKey.get().getPrimaryTableSchema())) {
                String change = String.format(COLUMN_FOREIGN_KEY_CHANGED,
                        localForeignKey.getName(),
                        localForeignKey.getColumnName(),
                        localForeignKey.getTableName(),
                        "Primary Key Schema Name",
                        localForeignKey.getPrimaryTableSchema(),
                        targetForeignKey.get().getPrimaryTableSchema());

                changeLogs.add(change);
            }
        }

        Set<String> localFKNames = localForeignKeys.stream().map(ForeignKey::getName).collect(Collectors.toSet());
        targetForeignKeys
                .stream()
                .filter(foreignKey -> !localFKNames.contains(foreignKey.getName()))
                .forEach(foreignKey -> {
                    String change = String.format(COLUMN_FOREIGN_KEY_ADDED,
                            foreignKey.getName(),
                            foreignKey.getColumnName(),
                            foreignKey.getTableName());
                    changeLogs.add(change);
                });

        return changeLogs;
    }

    private Optional<ForeignKey> getForeignKeyByName(List<ForeignKey> targetForeignKeys, ForeignKey localForeignKey) {
        return targetForeignKeys.stream().filter(foreignKey -> Objects.equals(localForeignKey.getName(), foreignKey.getName())).findFirst();
    }

    private Optional<Index> getIndexByName(List<Index> targetIndices, Index localIndex) {
        return targetIndices.stream().filter(index -> Objects.equals(localIndex.getName(), index.getName())).findFirst();
    }

    private Optional<Column> getColumn(String columnName, Table targetTable) {
        return targetTable.getColumns().stream().filter(targetColumn -> targetColumn.getName().equals(columnName)).findFirst();
    }

    private Optional<Table> getTable(String tableName, Database targetValue) {
        return targetValue.getTables().stream().filter(targetTable -> targetTable.getName().equals(tableName)).findFirst();
    }

    private Optional<View> getView(String viewName, Database targetValue) {
        return targetValue.getViews().stream().filter(targetView -> targetView.getName().equals(viewName)).findFirst();
    }
}
