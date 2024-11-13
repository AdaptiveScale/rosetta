package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

import javax.swing.text.html.Option;
import java.util.Optional;

public class DefaultAssertionSqlGenerator extends BaseAssertionSqlGenerator {

    @Override
    String prepareSql(Connection connection, Table table, Column column, AssertTest assertion, String whereClauseCondition) {
        return String.format("Select Count(*) from %s where %s %s",
                table.getName(),
                Optional.ofNullable(assertion.getColumnDef())
                    .orElse(column.getName()),
                whereClauseCondition
        );
    }

    String castValue(Object value, Column column) {
        return value == null ? null : "'" + value + "'";
    }
}
