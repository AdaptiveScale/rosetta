package com.adaptivescale.rosetta.diff.kinetica;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Index;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.models.ColumnProperties;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.diff.DefaultTester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.DIFF_TESTER
)
public class KineticaTester extends DefaultTester {

    private static final String TABLE_COLUMNS_CHANGED_FORMAT = "Table Changed: Table '%s' columns changed in the target database.";
    private static final String TABLE_REMOVED_FORMAT = "Table '%s' exists in the model, but it does not exist in the target database.";
    private static final String TABLE_ADDED_FORMAT = "Table '%s' does not exist in the model, but it exists in the target database.";

    private static final String COLUMN_CHANGED_FORMAT = "Column Changed: Column '%s' in table '%s' changed '%s'. New value: '%s', old value: '%s'";
    private static final String COLUMN_REMOVED_FORMAT = "Column '%s' in table '%s' exists in the model, but it does not exist in the target database.";
    private static final String COLUMN_ADDED_FORMAT = "Column '%s' in table '%s' does not exist in the model, but it exists in the target database.";

    private static final String COLUMN_FOREIGN_KEY_CHANGED = "Foreign Key Changed: FK '%s' on Column '%s' in table '%s' changed '%s'. New value: '%s', old value: '%s'";
    private static final String COLUMN_FOREIGN_KEY_REMOVED = "ForeignKey '%s' on Column '%s' in table '%s' exists in the model, but it does not exist in the target database.";
    private static final String COLUMN_FOREIGN_KEY_ADDED = "ForeignKey '%s' on Column '%s' in table '%s' does not exist in the model, but it exists in the target database.";

    private static final String INDEX_CHANGED_FORMAT = "Index Changed: Index '%s'";
    private static final String INDEX_REMOVED_FORMAT = "Index '%s' exists in the model, but it does not exist in the target database.";
    private static final String INDEX_ADDED_FORMAT = "Index '%s' does not exist in the model, but it exists in the target database.";

    private static final String VIEW_COLUMNS_CHANGED_FORMAT = "View Changed: View '%s' columns changed";
    private static final String VIEW_REMOVED_FORMAT = "View '%s' exists in the model, but it does not exist in the target database.";
    private static final String VIEW_ADDED_FORMAT = "View '%s' does not exist in the model, but it exists in the target database.";

    private static final String INTERLEAVED_CHANGED_FORMAT = "Interleaved Changed: Table '%s'";
    private static final String INTERLEAVED_REMOVED_FORMAT = "Interleaved '%s' table exists in the model, but it does not exist in the target database.";
    private static final String INTERLEAVED_ADDED_FORMAT = "Interleaved '%s' table does not exist in the model, but it exists in the target database.";

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

            List<String> tableInterleaveChanges = checkForInterleaveChanges(table, targetTable.get());
            changes.addAll(tableInterleaveChanges);

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

                if(!areColumnPropertiesEqual(localColumn.getColumnProperties(), targetColumn.get().getColumnProperties())) {
                    String result = String.format(COLUMN_CHANGED_FORMAT, localColumn.getName(),
                            table.getName(), "Column Properties", localColumn.columnPropertiesAsString(),
                            targetColumn.get().columnPropertiesAsString());
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

    private List<String> checkForInterleaveChanges(Table localTable, Table targetTable) {
        List<String> changes = new ArrayList<>();
        if (localTable.getInterleave() != null && targetTable.getInterleave() == null) {
            changes.add(String.format(INTERLEAVED_REMOVED_FORMAT, localTable.getName()));
        }
        if (localTable.getInterleave() == null && targetTable.getInterleave() != null) {
            changes.add(String.format(INTERLEAVED_ADDED_FORMAT, localTable.getName()));
        }

        if (localTable.getInterleave() != null &&
                targetTable.getInterleave() != null &&
                !localTable.getInterleave().equals(targetTable.getInterleave())) {
            changes.add(String.format(INTERLEAVED_CHANGED_FORMAT, localTable.getName()));
        }

        return changes;
    }

    private void testViews(Database localValue, Database targetValue, List<String> changes) {
        Collection<View> localViews = Optional.ofNullable(localValue.getViews())
                .orElse(Collections.emptyList());

        //do we need to check for root properties if are changed
        for (View view : localViews) {
            List<String> columnsChangesLogs = new ArrayList<>();
            Optional<View> targetView = getView(view.getName(), targetValue);
            if (targetView.isEmpty()) {
                //this view is removed
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
        Set<String> localViewName = localViews.stream().map(View::getName).collect(Collectors.toSet());
        List<View> viewsAdded = targetValue
                .getViews()
                .stream()
                .filter(view -> !localViewName.contains(view.getName()))
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

    private static boolean areColumnPropertiesEqual(List<ColumnProperties> listLocal, List<ColumnProperties> listTarget) {
        if (listLocal.size() != listTarget.size()) {
            return false;
        }
        for (ColumnProperties prop1 : listLocal) {
            boolean found = false;
            for (ColumnProperties prop2 : listTarget) {
                if (Objects.equals(prop1, prop2)){
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
