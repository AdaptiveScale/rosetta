package com.adaptivescale.rosetta.ddl.targets.kinetica;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.Index;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.utils.TemplateEngine;
import lombok.extern.slf4j.Slf4j;

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

    private final static String VIEW_DROP_TEMPLATE = "kinetica/view/drop";

    private final static String VIEW_CREATE_TEMPLATE = "kinetica/view/create";

    private final static String VIEW_ALTER_TEMPLATE = "kinetica/view/alter";

    private final static List<String> RESERVED_SCHEMA_NAMES = List.of("ki_home");

    private static final String GEOSPATIAL_INDEX_FORMAT = "GEOSPATIAL INDEX (%s)";
    private static final String CHUNK_SKIP_INDEX_FORMAT = "CHUNK SKIP INDEX (%s)";
    private static final String CAGRA_INDEX_FORMAT = "%s INDEX (%s) WITH OPTIONS (INDEX_OPTIONS = '%s')";
    private static final String GENERIC_INDEX_FORMAT = "%s INDEX (%s)";
    private static final String ADD_FOREIGN_KEY_FORMAT = "FOREIGN KEY (\"%s\") REFERENCES \"%s\".\"%s\" (\"%s\") AS \"%s\"";

    private final ColumnSQLDecoratorFactory columnSQLDecoratorFactory = new KineticaColumnDecoratorFactory();

    @Override
    public String createColumn(Column column) {
        return columnSQLDecoratorFactory.decoratorFor(column).expressSQl();
    }

    @Override
    public String createTable(Table table, boolean dropTableIfExists) {
        Map<String, Object> createParams = new HashMap<>();

        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());

        List<String> foreignKeysForTable = createForeignKeysForTable(table);
        Optional<String> primaryKeysForTable = createPrimaryKeysForTable(table);
        List<String> indicesForTable = getIndicesForTable(table);
        primaryKeysForTable.ifPresent(definitions::add);
        definitions.addAll(foreignKeysForTable);
        String definitionAsString = String.join(", ", definitions);

        StringBuilder stringBuilder = new StringBuilder();
        if (dropTableIfExists) {
            stringBuilder.append(dropTable(table));
        }

        String tableType = extractTableType(table);
        if (table.getAdditionalProperties().containsKey("partitions")) {
            String partitions = table.getPropertyAsString("partitions");
            createParams.put("partitions", partitions);
        }

        createParams.put("tableType", tableType);
        createParams.put("schemaName", table.getSchema());
        createParams.put("tableName", table.getName());
        createParams.put("tableCode", definitionAsString);
        createParams.put("indices", String.join("\n", indicesForTable));
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
                    .filter(s -> !RESERVED_SCHEMA_NAMES.contains(s))
                    .map(this::createSchema)
                    .collect(Collectors.joining())
            );
            stringBuilder.append("\r");
        }

        stringBuilder.append(database.getTables()
            .stream()
            .map(table -> createTable(table, dropTableIfExists))
            .collect(Collectors.joining("\r\r")));

        stringBuilder.append(database.getViews()
             .stream()
             .map(view -> createView(view, dropTableIfExists))
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

    @Override
    public String createView(View view, boolean dropViewIfExists) {
        StringBuilder builder = new StringBuilder();

        if (dropViewIfExists) {
            dropView(view);
            builder.append(dropView(view));
        }

        Map<String, Object> createParams = new HashMap<>();
        createParams.put("schemaName", view.getSchema());
        createParams.put("viewName", view.getName());
        createParams.put("materialized", view.getMaterializedString());
        createParams.put("viewCode", view.getCode());
        builder.append(TemplateEngine.process(VIEW_CREATE_TEMPLATE, createParams));

        return builder.toString();
    }

    @Override
    public String dropView(View actual) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", actual.getSchema());
        params.put("viewName", actual.getName());
        return TemplateEngine.process(VIEW_DROP_TEMPLATE, params);
    }

    @Override
    public String alterView(View expected, View actual) {
        Map<String, Object> createParams = new HashMap<>();
        createParams.put("schemaName", expected.getSchema());
        createParams.put("viewName", expected.getName());
        createParams.put("materialized", expected.getMaterializedString());
        createParams.put("viewCode", expected.getCode());
        return TemplateEngine.process(VIEW_ALTER_TEMPLATE, createParams);
    }

    private Optional<String> createPrimaryKeysForTable(Table table) {
        List<String> primaryKeys = table
            .getColumns()
            .stream()
            .filter(Column::isPrimaryKey)
            .sorted((o1, o2) -> o1.getPrimaryKeySequenceId() < o2.getPrimaryKeySequenceId() ? -1 : 1)
            .map(pk -> String.format(DEFAULT_WRAPPER+"%s"+DEFAULT_WRAPPER, pk.getName()))
            .collect(Collectors.toList());

        if (primaryKeys.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of("PRIMARY KEY (" + String.join(", ", primaryKeys) + ")");
    }

    private List<String> getIndicesForTable(Table table) {
        List<Index> result = table.getIndices();
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> generateIndexStatement = generateIndexStatements(result.stream().findFirst().get().getColumnNames());
        return generateIndexStatement;
    }

    public static List<String> generateIndexStatements(List<String> values) {
        List<String> indexStatements = new ArrayList<>();

        for (String value : values) {
            String statement = generateIndexStatement(value);
            indexStatements.add(statement);
        }

        return indexStatements;
    }

    private static String generateIndexStatement(String value) {
        if (value.contains("@")) {
            String[] parts = value.split("@");
            String type = parts[0].toUpperCase();
            String[] ids = parts[1].split(":");
            String joinedIds = String.join(", ", ids);

            switch (type.toLowerCase()) {
                case "geospatial":
                    return String.format(GEOSPATIAL_INDEX_FORMAT, joinedIds);

                case "chunk_skip":
                    return String.format(CHUNK_SKIP_INDEX_FORMAT, joinedIds);

                case "cagra":
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("Invalid format. Expected 3 parts separated by '@'.");
                    }

                    String indexName = parts[1];
                    String options = parts[2];

                    // Split options by "," and reformat them
                    String[] optionPairs = options.split(",");
                    StringBuilder optionsBuilder = new StringBuilder();

                    for (String option : optionPairs) {
                        // Split the key-value pair by ":"
                        String[] keyValue = option.split(":");
                        if (keyValue.length == 2) {
                            optionsBuilder.append(keyValue[0])
                                    .append(": ")
                                    .append(keyValue[1])
                                    .append(", ");
                        } else {
                            throw new IllegalArgumentException("Invalid option format. Expected key:value pairs.");
                        }
                    }

                    if (optionsBuilder.length() > 0) {
                        optionsBuilder.setLength(optionsBuilder.length() - 2);
                    }

                    // Construct the final string using the CAGRA_INDEX_FORMAT constant
                    return String.format(CAGRA_INDEX_FORMAT, type, indexName, optionsBuilder.toString());

                default:
                    return String.format(GENERIC_INDEX_FORMAT, type, joinedIds);
            }
        } else {
            return String.format("INDEX (%s)", value);
        }
    }

    private Optional<String> foreignKeys(Table table) {
        String result = table.getColumns().stream()
            .filter(column -> column.getForeignKeys() != null && !column.getForeignKeys().isEmpty())
            .map(this::createForeignKeys).collect(Collectors.joining());

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    private List<String> createForeignKeysForTable(Table table) {
        List<String> result = new ArrayList<>();
        table.getColumns().forEach(column -> {
            if (column.getForeignKeys() != null && !column.getForeignKeys().isEmpty()) {
                column.getForeignKeys().forEach(foreignKey -> result.add(
                    String.format(
                        ADD_FOREIGN_KEY_FORMAT,
                        column.getName(),
                        foreignKey.getPrimaryTableSchema(),
                        foreignKey.getPrimaryTableName(),
                        foreignKey.getPrimaryColumnName(),
                        foreignKey.getName()
                    )
                ));
            }
        });
        return result;
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

    private String extractTableType(Table table) {
        List<String> tableTypes = new ArrayList<>();

        // for Kinetica when shardKind is R it means that the table is REPLICATED
        // when persistence is T it means that the table is TEMPORARY
        if (table.getAdditionalProperties().containsKey("shard_kind") && table.getPropertyAsString("shard_kind").equals("R")) {
            tableTypes.add("REPLICATED");
        }
        if (table.getAdditionalProperties().containsKey("persistence") && table.getPropertyAsString("persistence").equals("T")) {
            tableTypes.add("TEMP");
        }
        tableTypes.add("TABLE");

        return String.join(" ", tableTypes);
    }
}
