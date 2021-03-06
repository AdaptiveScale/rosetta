package com.adaptivescale.rosetta.ddl.targets.snowflake;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;

import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SnowflakeDDLGenerator implements DDL {

    private final ColumnSQLDecoratorFactory columnSQLDecoratorFactory;

    public SnowflakeDDLGenerator() {
        columnSQLDecoratorFactory = new SnowflakeColumnDecoratorFactory();
    }

    @Override
    public String createColumn(Column column) {
        return columnSQLDecoratorFactory.decoratorFor(column).expressSQl();
    }

    @Override
    public String createTable(Table table) {
        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());

        Optional<String> primaryKeysForTable = createPrimaryKeysForTable(table);
        primaryKeysForTable.ifPresent(definitions::add);
        String definitionAsString = String.join(", ", definitions);

        return "CREATE TABLE "
                + ((table.getSchema() == null || table.getSchema().isEmpty()) ? "" : table.getSchema() + ".")
                + table.getName()
                + "("
                + definitionAsString
                + ");";
    }

    @Override
    public String createDataBase(Database database) {
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> schemas = database.getTables().stream().map(Table::getSchema)
                .filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());
        if (!schemas.isEmpty()) {
            stringBuilder.append(
                    schemas
                            .stream()
                            .map(schema -> "create schema " + schema)
                            .collect(Collectors.joining(";\r\r"))

            );
            stringBuilder.append(";\r");
        }

        stringBuilder.append(database.getTables()
                .stream()
                .map(this::createTable)
                .collect(Collectors.joining("\r\r")));

        String foreignKeys = database
                .getTables()
                .stream()
                .map(this::foreignKeys)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("\r"));

        if(!foreignKeys.isEmpty()){
            stringBuilder.append("\r").append(foreignKeys).append("\r");
        }

        return stringBuilder.toString();
    }

    private Optional<String> createPrimaryKeysForTable(Table table) {
        List<String> primaryKeys = table
                .getColumns()
                .stream()
                .filter(Column::isPrimaryKey)
                .sorted((o1, o2) -> o1.getPrimaryKeySequenceId() < o2.getPrimaryKeySequenceId() ? -1 : 1)
                .map(Column::getName)
                .collect(Collectors.toList());

        if (primaryKeys.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
    }

    private Optional<String> foreignKeys(Table table) {
        String result = table.getColumns().stream()
                .filter(column -> column.getForeignKeys() != null && !column.getForeignKeys().isEmpty())
                .map(this::foreignKey).collect(Collectors.joining());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    //ALTER TABLE rosetta.contacts ADD CONSTRAINT contacts_fk FOREIGN KEY (contact_id) REFERENCES rosetta."user"(user_id);
    private String foreignKey(Column column) {
        return column.getForeignKeys().stream().map(foreignKey ->
                "ALTER TABLE " + ((foreignKey.getSchema() == null || foreignKey.getSchema().isEmpty()) ? "" : foreignKey.getSchema() + ".")
                        + foreignKey.getTableName() + " ADD CONSTRAINT "
                        + foreignKey.getName() + " FOREIGN KEY (" + foreignKey.getColumnName() + ") REFERENCES "
                        + ((foreignKey.getPrimaryTableSchema() == null || foreignKey.getPrimaryTableSchema().isEmpty()) ? "" : foreignKey.getPrimaryTableSchema() + ".")
                        + foreignKey.getPrimaryTableName()
                        + "(" + foreignKey.getPrimaryColumnName() + ")"
                        + foreignKeyDeleteRuleSanitation(foreignKeyDeleteRule(foreignKey)) + ";\r"
                ).collect(Collectors.joining());
    }

    private String foreignKeyDeleteRuleSanitation(String deleteRule) {
        if (deleteRule == null || deleteRule.isEmpty()) {
            return "";
        }
        return " " + deleteRule + " ";
    }

    protected String foreignKeyDeleteRule(ForeignKey foreignKey) {
        if (foreignKey.getDeleteRule() == null || foreignKey.getDeleteRule().isEmpty()) {
            return "";
        }
        switch (Integer.parseInt(foreignKey.getDeleteRule())) {
            case DatabaseMetaData.importedKeyCascade:
                return "ON DELETE CASCADE";
            case DatabaseMetaData.importedKeySetNull:
                return "ON DELETE SET NULL";
            case DatabaseMetaData.importedKeyNoAction:
                return "ON DELETE NO ACTION";
            case DatabaseMetaData.importedKeySetDefault:
            case DatabaseMetaData.importedKeyInitiallyDeferred:
            case DatabaseMetaData.importedKeyInitiallyImmediate:
            case DatabaseMetaData.importedKeyNotDeferrable:
            default:
                //todo add warn log
                return "";
        }
    }
}
