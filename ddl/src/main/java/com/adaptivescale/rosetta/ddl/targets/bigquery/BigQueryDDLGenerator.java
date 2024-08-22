package com.adaptivescale.rosetta.ddl.targets.bigquery;
import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
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

@Slf4j
@RosettaModule(
        name = "bigquery",
        type = RosettaModuleTypes.DDL_GENERATOR
)
public class BigQueryDDLGenerator implements DDL {
    private final static String TABLE_CREATE_TEMPLATE = "bigquerry/table/create";


    private final static String TABLE_DROP_TEMPLATE = "bigquerry/table/drop";

    private final static String SCHEMA_CREATE_TEMPLATE = "bigquerry/schema/create";

    private final static String COLUMN_ADD_TEMPLATE = "bigquerry/column/add";

    private final static String COLUMN_ALTER_TYPE_TEMPLATE = "bigquerry/column/alter_column_type";

    private final static String COLUMN_DROP_TEMPLATE = "bigquerry/column/drop";
    private static String VIEW_DROP_TEMPLATE = "bigquery/view/drop";
    private static String VIEW_CREATE_TEMPLATE = "bigquery/view/create";
    private static String VIEW_ALTER_TEMPLATE = "bigquery/view/alter";
    private final ColumnSQLDecoratorFactory columnSQLDecoratorFactory;

    public BigQueryDDLGenerator() {
        columnSQLDecoratorFactory = new BigQueryColumnDecoratorFactory();
    }

    @Override
    public String createColumn(Column column) {
        return columnSQLDecoratorFactory.decoratorFor(column).expressSQl();
    }

    @Override
    public String createTable(Table table, boolean dropTableIfExists) {
        Map<String, Object> createParams = new HashMap<>();

        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());

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

        return stringBuilder.toString();
    }

    @Override
    public String alterColumn(ColumnChange change) {
        Column actual = change.getActual();
        Column expected = change.getExpected();
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", change.getTable().getSchema());
        params.put("tableName", change.getTable().getName());
        params.put("columnName", expected.getName());

        if (!Objects.equals(expected.getTypeName(), actual.getTypeName())) {
            params.put("dataType", expected.getTypeName());
            return TemplateEngine.process(COLUMN_ALTER_TYPE_TEMPLATE, params);
        }

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
    public String createForeignKey(ForeignKey foreignKey) {
        return null;
    }

    @Override
    public String alterForeignKey(ForeignKeyChange change) {
        return null;
    }

    @Override
    public String dropForeignKey(ForeignKey actual) {
        return null;
    }

    @Override
    public String alterTable(Table expected, Table actual) {
        return null;
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
        createParams.put("viewCode", expected.getCode());
        return TemplateEngine.process(VIEW_ALTER_TEMPLATE, createParams);
    }

    private String createSchema(String schema) {
        Map<String, Object> params = new HashMap<>();
        params.put("schemaName", schema);
        return TemplateEngine.process(SCHEMA_CREATE_TEMPLATE, params);
    }
}
