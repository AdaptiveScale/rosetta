package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

public class BigQueryAssertionSqlGenerator extends BaseAssertionSqlGenerator {

    @Override
    String prepareSql(Connection connection, Table table, Column column, String whereClauseCondition) {
        String columnName = isArray(column) ? String.format("ARRAY_TO_STRING(%s,',')", column.getName()) : column.getName();
        return String.format("Select Count(*) from %s.%s.%s where %s %s",
                connection.getDatabaseName(),
                connection.getSchemaName(),
                table.getName(),
                columnName,
                whereClauseCondition);
    }

    private boolean isArray(Column column) {
        return "ARRAY".equals(column.getTypeName());
    }

    /**
     * Use method to prepare value for sql 
     *
     * @param value  to cast
     * @param column model column
     * @return BigQuery function
     */
    String castValue(Object value, Column column) {
        String stringValue = value == null ? null : "\"" + value + "\"";
        if (isArray(column)) {
            return stringValue;
        }
        return " CAST(" + stringValue + " as " + column.getTypeName() + ")";
    }
}
