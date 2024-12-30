package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

public class RedshiftAssertionSqlGenerator extends BaseAssertionSqlGenerator {
    @Override
    String prepareSql(Connection connection, Table table, Column column, AssertTest assertTest, String whereClauseCondition) {

        return String.format("Select Count(*) from \"%s\".\"%s\" where %s %s",
                table.getSchema(),
                table.getName(),
                column.getName(),
                whereClauseCondition);
    }

    String castValue(Object value, Column column) {
        return value == null ? null : "'" + value + "'";
    }
}
