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
        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());
        String definitionAsString = String.join(", ", definitions);

        StringBuilder builder = new StringBuilder();

        if (dropTableIfExists) {
            builder.append("DROP TABLE IF EXISTS ");
            if (table.getSchema() != null && !table.getSchema().isBlank()) {
                builder.append("`")
                        .append(table.getSchema())
                        .append("`.");
            }
            builder.append("`").append(table.getName()).append("`; \n");
        }

        if (table.getSchema() != null && !table.getSchema().isBlank()) {
            builder.append("CREATE SCHEMA IF NOT EXISTS " + table.getSchema()).append(";\n");
        }

        builder.append("CREATE TABLE ");

        if (table.getSchema() != null && !table.getSchema().isBlank()) {
            builder.append("`")
                    .append(table.getSchema())
                    .append("`.");
        }

        builder.append("`")
                .append(table.getName())
                .append("`")
                .append("(")
                .append(definitionAsString)
                .append(");");

        return builder.toString();
    }

    @Override
    public String createDatabase(Database database, boolean dropTableIfExists) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(database.getTables()
            .stream()
            .map(table -> createTable(table, dropTableIfExists))
            .collect(Collectors.joining("\r")));

        return stringBuilder.toString();
    }

    @Override
    public String alterColumn(ColumnChange change) {

        Column actual = change.getActual();
        Column expected = change.getExpected();

        if (!Objects.equals(expected.getTypeName(), actual.getTypeName())) {
            return String.format("ALTER TABLE %s.%s ALTER COLUMN %s SET DATA TYPE %s;", change.getTable().getSchema(),
                    change.getTable().getName(),
                    expected.getName(),
                    expected.getTypeName());
        }

        if (!Objects.equals(expected.isNullable(), actual.isNullable())) {
            if (expected.isNullable()) {
                return String.format("ALTER TABLE %s.%s ALTER COLUMN %s DROP NOT NULL;", change.getTable().getSchema(),
                        change.getTable().getName(),
                        expected.getName());
            } else {
                throw new RuntimeException("Operation not supported by BigQuery to alter column to not null!");
            }
        }
        log.info("No action taken for changes detected in column: {}.{}.{}", change.getTable().getSchema(),
                change.getTable().getName(),
                expected.getName());
        return "";
    }

    @Override
    public String dropColumn(ColumnChange change) {
        return String.format("ALTER TABLE %s.%s DROP COLUMN %s;", change.getTable().getSchema(), change.getTable().getName(), change.getActual().getName());
    }

    @Override
    public String addColumn(ColumnChange change) {
        String columnNameWithType = columnSQLDecoratorFactory.decoratorFor(change.getExpected()).expressSQl();
        return String.format("ALTER TABLE %s.%s ADD COLUMN %s;", change.getTable().getSchema(), change.getTable().getName(), columnNameWithType);
    }

    @Override
    public String dropTable(Table actual) {
        return String.format("DROP TABLE %s.%s;", actual.getSchema(), actual.getName());
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
}
