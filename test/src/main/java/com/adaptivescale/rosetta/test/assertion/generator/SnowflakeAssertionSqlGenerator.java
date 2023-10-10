package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

public class SnowflakeAssertionSqlGenerator extends BaseAssertionSqlGenerator {

    @Override
    String prepareSql(Connection connection, Table table, Column column, String whereClauseCondition) {
        return String.format("Select Count(*) from \"%s\".\"%s\".\"%s\" where \"%s\" %s",
                connection.getDatabaseName(),
                connection.getSchemaName(),
                table.getName(),
                column.getName(), whereClauseCondition);
    }

    /**
     * Wrap value with single quote
     * @param value  to cast
     * @param column model column
     * @return single quoted value
     */
     String castValue(Object value, Column column) {
        return value == null ? null : "'" + value + "'";
    }
}
