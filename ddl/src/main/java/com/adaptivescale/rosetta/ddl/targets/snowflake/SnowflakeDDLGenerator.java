package com.adaptivescale.rosetta.ddl.targets.snowflake;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;

import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public String createDatabase(Database database) {
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

        if (!foreignKeys.isEmpty()) {
            stringBuilder.append("\r").append(foreignKeys).append("\r");
        }

        return stringBuilder.toString();
    }

    //for change optimal decision is to drop and create again
    @Override
    public String alterForeignKey(ForeignKeyChange change) {
       return dropForeignKey(change.getActual()) + "\r" + createForeignKey(change.getExpected());
    }

    @Override
    public String dropForeignKey(ForeignKey actual) {
        StringBuilder stringBuilder = new StringBuilder().append("ALTER TABLE ");
        if (actual.getSchema() != null && !actual.getSchema().isEmpty()) {
            stringBuilder.append(escapeName(actual.getSchema())).append(".");
        }

        stringBuilder
                .append(escapeName(actual.getTableName()))
                .append(" DROP CONSTRAINT ")
                .append(escapeName(actual.getName()))
                .append(";");

        return stringBuilder.toString();
    }

    @Override
    public String alterColumn(ColumnChange change) {
        Column actual = change.getActual();
        Column expected = change.getExpected();

        if (!Objects.equals(expected.getTypeName(), actual.getTypeName())) {
            return String.format("ALTER TABLE %s ALTER  \"%s\" SET DATA TYPE %s;",
                    tableNameWithSchema(change.getTable()),
                    expected.getName(),
                    expected.getTypeName());
        }

        if (!Objects.equals(expected.isNullable(), actual.isNullable())) {
            if (expected.isNullable()) {
                return String.format("ALTER TABLE %s ALTER  \"%s\" DROP NOT NULL;",
                        tableNameWithSchema(change.getTable()),
                        expected.getName());
            } else {
                return String.format("ALTER TABLE %s ALTER  \"%s\" SET NOT NULL;",
                        tableNameWithSchema(change.getTable()),
                        expected.getName());
            }
        }


        //todo what
        return null;
    }

    @Override
    public String dropColumn(ColumnChange change) {
        return "ALTER TABLE " + tableNameWithSchema(change.getTable()) + " DROP " + escapeName(change.getActual().getName()) + ";";
    }

    @Override
    public String addColumn(ColumnChange change) {
        Table table = change.getTable();
        String columnNameWithType = columnSQLDecoratorFactory.decoratorFor(change.getExpected()).expressSQl();
        String tableName = ((table.getSchema() == null || table.getSchema().isEmpty()) ? "" : table.getSchema() + ".") + table.getName();
        return "Alter table " + tableName + " add " + columnNameWithType;
    }

    @Override
    public String dropTable(Table actual) {
        return "DROP TABLE " + tableNameWithSchema(actual) + ";";
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
                .map(this::createForeignKey).collect(Collectors.joining());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    private String createForeignKey(Column column) {
        //ALTER TABLE rosetta.contacts ADD CONSTRAINT contacts_fk FOREIGN KEY (contact_id) REFERENCES rosetta."user"(user_id);
        return column.getForeignKeys().stream().map(this::createForeignKey).collect(Collectors.joining());
    }

    @Override
    public String createForeignKey(ForeignKey foreignKey) {
//   ALTER TABLE "FBAL"."PLAYER" ADD CONSTRAINT "PLAYER_FK" FOREIGN KEY ("POSITION_ID") REFERENCES "FBAL"."Position"(ID) ON DELETE NO ACTION ;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ALTER TABLE ");

        if (foreignKey.getSchema() != null && !foreignKey.getSchema().isEmpty()) {
            stringBuilder.append(escapeName(foreignKey.getSchema())).append(".");
        }

        stringBuilder
                .append(escapeName(foreignKey.getTableName()))
                .append(" ADD CONSTRAINT ")
                .append(escapeName(foreignKey.getName()))
                .append(" FOREIGN KEY (")
                .append(escapeName(foreignKey.getColumnName()))
                .append(") REFERENCES ");

        if (foreignKey.getPrimaryTableSchema() != null && foreignKey.getPrimaryTableSchema().isEmpty()) {
            stringBuilder.append("\"").append(foreignKey.getPrimaryTableSchema()).append("\".");
        }

        stringBuilder
                .append(escapeName(foreignKey.getPrimaryTableName()))
                .append("(")
                .append(escapeName(foreignKey.getPrimaryColumnName()))
                .append(")");

        String deleteRule = foreignKeyDeleteRule(foreignKey);

        if (deleteRule != null) {
            stringBuilder.append(" ").append(deleteRule);
        }
        stringBuilder.append(";\r");

        return stringBuilder.toString();
    }

    private String escapeName(String name) {
        return "\"" + name + "\"";
    }

    protected String foreignKeyDeleteRule(ForeignKey foreignKey) {
        if (foreignKey.getDeleteRule() == null || foreignKey.getDeleteRule().isEmpty()) {
            return null;
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
                return null;
        }
    }

    private String tableNameWithSchema(Table table) {
        StringBuilder builder = new StringBuilder();

        if (table.getSchema() != null && !table.getSchema().isEmpty()) {
            builder.append("\"").append(table.getSchema()).append("\".");
        }
        builder.append("\"").append(table.getName()).append("\"");

        return builder.toString();
    }
}
