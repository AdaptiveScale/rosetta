package com.adaptivescale.rosetta.ddl.targets.kinetica;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.utils.TemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;

import static com.adaptivescale.rosetta.ddl.targets.kinetica.Constants.DEFAULT_WRAPPER;

@Slf4j
@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.DDL_GENERATOR
)
public class KineticaDDLGenerator implements DDL {

    private final static String TABLE_CREATE_TEMPLATE = "kinetica/table/create";

    private final static String TABLE_ALTER_TEMPLATE = "kinetica/table/alter";

    private final static String TABLE_DROP_TEMPLATE = "kinetica/table/drop";

    private final static String SCHEMA_CREATE_TEMPLATE = "kinetica/schema/create";

    private final static String FOREIGN_KEY_CREATE_TEMPLATE = "kinetica/foreignkey/create";

    private final static String FOREIGN_KEY_DROP_TEMPLATE = "kinetica/foreignkey/drop";

    private final static String COLUMN_ADD_TEMPLATE = "kinetica/column/add";

    private final static String COLUMN_MODIFY_TEMPLATE = "kinetica/column/modify";

    private final static String COLUMN_DROP_TEMPLATE = "kinetica/column/drop";

    private final ColumnSQLDecoratorFactory columnSQLDecoratorFactory = new KineticaColumnDecoratorFactory();

    @Override
    public String createColumn(Column column) {
        return columnSQLDecoratorFactory.decoratorFor(column).expressSQl();
    }

    @Override
    public String createTable(Table table, boolean dropTableIfExists) {
        Map<String, Object> createParams = new HashMap<>();

        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());

        List<String> foreignKeysForTable = getForeignKeysColumnNames(table);
        Optional<String> primaryKeysForTable = createPrimaryKeysForTable(table, foreignKeysForTable);
        primaryKeysForTable.ifPresent(definitions::add);
        String definitionAsString = String.join(", ", definitions);

        StringBuilder stringBuilder = new StringBuilder();
        if (dropTableIfExists) {
            stringBuilder.append(dropTable(table));
        }

        createParams.put("schemaName", table.getSchema());
        createParams.put("tableName", table.getName());
        createParams.put("tableCode", definitionAsString);
        stringBuilder.append(TemplateEngine.process(TABLE_CREATE_TEMPLATE, createParams));

        return stringBuilder.toString();
    }

    @Override
    public String createTableSchema(Table table) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", table.getSchema());
        return TemplateEngine.process(SCHEMA_CREATE_TEMPLATE, params);
    }

    @Override
    public String createDatabase(Database database, boolean dropTableIfExists) {
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> schemas = database.getTables().stream().map(Table::getSchema).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());
        if (!schemas.isEmpty()) {
            stringBuilder.append(
                schemas
                    .stream()
                    .map(this::createSchema)
                    .collect(Collectors.joining())
            );
            stringBuilder.append("\r");
        }

        stringBuilder.append(database.getTables()
            .stream()
            .map(table -> createTable(table, dropTableIfExists))
            .collect(Collectors.joining("\r\r")));

        //TODO: Check if we can enable foreign keys in Kinetica
        //Disable temporarily the foreign keys in Kinetica
//        String foreignKeys = database
//            .getTables()
//            .stream()
//            .map(this::foreignKeys)
//            .filter(Optional::isPresent)
//            .map(Optional::get)
//            .collect(Collectors.joining("\r"));
//
//        if (!foreignKeys.isEmpty()) {
//            stringBuilder.append("\r").append(foreignKeys).append("\r");
//        }

        return stringBuilder.toString();
    }

    @Override
    public String createForeignKey(ForeignKey foreignKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", foreignKey.getSchema());
        params.put("tableName", foreignKey.getTableName());
        params.put("foreignkeyColumn", foreignKey.getColumnName());
        params.put("primaryTableSchema", foreignKey.getPrimaryTableSchema());
        params.put("primaryTableName", foreignKey.getPrimaryTableName());
        params.put("foreignKeyPrimaryColumnName", foreignKey.getPrimaryColumnName());
        params.put("foreignkeyName", foreignKey.getName());
        return TemplateEngine.process(FOREIGN_KEY_CREATE_TEMPLATE, params);
    }

    @Override
    public String alterColumn(ColumnChange change) {
        Table table = change.getTable();
        Column actual = change.getActual();
        Column expected = change.getExpected();

        if (!Objects.equals(expected.getTypeName(), actual.getTypeName())
                || !Objects.equals(expected.isNullable(), actual.isNullable())
                || !Objects.equals(expected.getColumnProperties(), actual.getColumnProperties())) {

            Map<String, Object> params = new HashMap<>();
            params.put("schemaName", table.getSchema());
            params.put("tableName", table.getName());
            params.put("columnDefinition", columnSQLDecoratorFactory.decoratorFor(expected).expressSQl());
            return TemplateEngine.process(COLUMN_MODIFY_TEMPLATE, params);
        }

        log.info("No action taken for changes detected in column: {}.{}.{}", change.getTable().getSchema(),
                change.getTable().getName(),
                expected.getName());
        return "";
    }

    @Override
    public String dropColumn(ColumnChange change) {
        Table table = change.getTable();
        Column actual = change.getActual();

        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", table.getSchema());
        params.put("tableName", table.getName());
        params.put("columnName", actual.getName());
        return TemplateEngine.process(COLUMN_DROP_TEMPLATE, params);
    }

    @Override
    public String addColumn(ColumnChange change) {
        Table table = change.getTable();
        Column expected = change.getExpected();

        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", table.getSchema());
        params.put("tableName", table.getName());
        params.put("columnDefinition", columnSQLDecoratorFactory.decoratorFor(expected).expressSQl());
        return TemplateEngine.process(COLUMN_ADD_TEMPLATE, params);
    }

    @Override
    public String dropTable(Table actual) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", actual.getSchema());
        params.put("tableName", actual.getName());
        return TemplateEngine.process(TABLE_DROP_TEMPLATE, params);
    }

    @Override
    public String alterForeignKey(ForeignKeyChange change) {
        return "";
    }

    @Override
    public String dropForeignKey(ForeignKey actual) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", actual.getSchema());
        params.put("tableName", actual.getTableName());
        params.put("foreignkeyName", actual.getName());
        return TemplateEngine.process(FOREIGN_KEY_DROP_TEMPLATE, params);
    }

    @Override
    public String alterTable(Table expected, Table actual) {
        return "";
    }

    private Optional<String> createPrimaryKeysForTable(Table table, List<String> foreignKeysForTable) {
        List<String> primaryKeys = table
            .getColumns()
            .stream()
            .filter(Column::isPrimaryKey)
            .sorted((o1, o2) -> o1.getPrimaryKeySequenceId() < o2.getPrimaryKeySequenceId() ? -1 : 1)
            .map(pk -> String.format(DEFAULT_WRAPPER+"%s"+DEFAULT_WRAPPER, pk.getName()))
            .collect(Collectors.toList());

        //TODO: Enable this with foreign key functionality
//        primaryKeys.addAll(foreignKeysForTable);

        if (primaryKeys.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
    }

    private Optional<String> foreignKeys(Table table) {
        String result = table.getColumns().stream()
            .filter(column -> column.getForeignKeys() != null && !column.getForeignKeys().isEmpty())
            .map(this::createForeignKeys).collect(Collectors.joining());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    private List<String> getForeignKeysColumnNames(Table table) {
        return table.getColumns().stream()
            .filter(column -> column.getForeignKeys() != null && !column.getForeignKeys().isEmpty())
            .map(Column::getName)
            .map(fk -> String.format(DEFAULT_WRAPPER+"%s"+DEFAULT_WRAPPER, fk))
            .collect(Collectors.toList());
    }

    private String createForeignKeys(Column column) {
        return column.getForeignKeys().stream().map(this::createForeignKey).collect(Collectors.joining());
    }

    private String handleNullSchema(String schema, String tableName) {
        return ((schema == null || schema.isEmpty()) ? " " : (" "+ DEFAULT_WRAPPER + schema + DEFAULT_WRAPPER +".")) + DEFAULT_WRAPPER + tableName + DEFAULT_WRAPPER;
    }

    private String foreignKeyDeleteRuleSanitation(String deleteRule) {
        if (deleteRule == null || deleteRule.isEmpty()) {
            return "";
        }
        return " " + deleteRule + " ";
    }

    private String createSchema(String schema) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", schema);
        return TemplateEngine.process(SCHEMA_CREATE_TEMPLATE, params);
    }
}
