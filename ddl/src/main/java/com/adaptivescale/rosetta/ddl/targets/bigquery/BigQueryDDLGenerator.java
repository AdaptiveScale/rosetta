package com.adaptivescale.rosetta.ddl.targets.bigquery;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.change.model.ColumnChange;
import com.adaptivescale.rosetta.ddl.change.model.ForeignKeyChange;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;

import java.util.*;
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
    public String createTable(Table table) {
        List<String> definitions = table.getColumns().stream().map(this::createColumn).collect(Collectors.toList());
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
                            .map(schema -> "CREATE SCHEMA IF NOT EXISTS " + schema)
                            .collect(Collectors.joining(";\r\r"))

            );
            stringBuilder.append(";\r");
        }

        stringBuilder.append(database.getTables()
                .stream()
                .map(this::createTable)
                .collect(Collectors.joining("\r\r")));

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

        //todo throw or log warning
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
}
