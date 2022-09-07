package com.adaptivescale.rosetta.ddl.targets.bigquery;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BigQueryDDLGenerator implements DDL {
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

        builder.append("CREATE TABLE ");

        if (table.getSchema() != null && !table.getSchema().isBlank()) {
            builder.append("`")
                    .append(table.getSchema())
                    .append("`.");
        }

        builder.append(table.getName())
                .append("(")
                .append(definitionAsString)
                .append(");");

        return builder.toString();
    }

    @Override
    public String createDataBase(Database database, boolean dropTableIfExists) {
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> schemas = database.getTables().stream().map(Table::getSchema)
                .filter(s -> s != null && !s.isEmpty()).collect(Collectors.toSet());
        if (!schemas.isEmpty()) {
            stringBuilder.append(
                    schemas
                            .stream()
                            .map(schema -> "CREATE SCHEMA IF NOT EXISTS " + schema)
                            .collect(Collectors.joining(";\r\r"))

            );
            stringBuilder.append(";\r");
        }

        stringBuilder.append(database.getTables()
                .stream()
                .map(table -> createTable(table, dropTableIfExists))
                .collect(Collectors.joining("\r\r")));

        return stringBuilder.toString();
    }
}
