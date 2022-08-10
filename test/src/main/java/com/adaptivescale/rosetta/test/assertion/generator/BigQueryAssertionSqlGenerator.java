package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

public class BigQueryAssertionSqlGenerator extends BaseAssertionSqlGenerator {

    @Override
    String prepareSql(Connection connection, Table table, Column column, String whereClauseCondition) {
        return String.format("Select Count(*) from %s.%s.%s where %s %s",
                connection.getDatabaseName(),
                connection.getSchemaName(),
                table.getName(),
                column.getName(), whereClauseCondition);
    }

    /**
     * Use BigQuery function CAST ( x as y) to cast data types
     *
     * @param value  to cast
     * @param column model column
     * @return BigQuery function
     */
     String castValue(Object value, Column column) {
        String stringValue = value == null ? null : "\"" + value + "\"";
        return " CAST(" + stringValue + " as " + column.getTypeName() + ")";
    }
}
