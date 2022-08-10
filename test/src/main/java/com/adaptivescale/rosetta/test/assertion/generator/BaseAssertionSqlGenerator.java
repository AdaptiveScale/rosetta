package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.test.assertion.AssertionSqlGenerator;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseAssertionSqlGenerator implements AssertionSqlGenerator {

    @Override
    public String generateSql(Connection connection, Table table, Column column, AssertTest assertion) {
        String whereClauseCondition = prepareWhereCondition(column, assertion);
        return prepareSql(connection, table, column, whereClauseCondition);
    }

    String prepareWhereCondition(Column column, AssertTest assertion) {
        String whereClauseCondition;

        if (assertion.getValue() == null) {
            if (!allowedOperatorsForNullValue(assertion)) {
                throw new RuntimeException(String.format("Operator %s is not allowed with value null", assertion.getValue()));
            }
            whereClauseCondition = AssertTest.EQUAL.equals(assertion.getOperator()) ? "is null" : "is not null";
        } else {
            whereClauseCondition = String.format("%s %s", assertion.getOperator(), handleOperator(assertion, column));
        }
        return whereClauseCondition;
    }

    abstract String prepareSql(Connection connection, Table table, Column column, String whereClauseCondition);

    String handleOperator(AssertTest assertion, Column column) {
        if (AssertTest.IN.equals(assertion.getOperator())) {
            if (assertion.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                String values = ((List<Object>) assertion.getValue())
                        .stream()
                        .map(o -> castValue(o, column))
                        .collect(Collectors.joining(","));
                return String.format("(%s)", values);
            }
        }
        return castValue(assertion.getValue(), column);
    }

    abstract String castValue(Object value, Column column);

    boolean allowedOperatorsForNullValue(AssertTest assertion) {
        return AssertTest.EQUAL.equals(assertion.getOperator()) || AssertTest.NOT_EQUAL.equals(assertion.getOperator());
    }
}
