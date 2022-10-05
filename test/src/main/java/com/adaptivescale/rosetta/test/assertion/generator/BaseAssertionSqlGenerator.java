package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.enums.OperatorEnum;
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
        if (assertion.getOperator().equalsIgnoreCase(OperatorEnum.IS_NULL.getName()) || assertion.getOperator().equalsIgnoreCase(OperatorEnum.IS_NOT_NULL.getName())) {
            return assertion.getOperator();
        }

        if (assertion.getValue() == null) {
            if (!allowedOperatorsForNullValue(assertion)) {
                throw new RuntimeException(String.format("Operator %s is not allowed with value null", assertion.getValue()));
            }

            return OperatorEnum.EQUAL.getName().equalsIgnoreCase(assertion.getOperator()) ? "is null" : "is not null";
        }

        return String.format("%s %s", assertion.getOperator(), handleOperator(assertion, column));
    }

    abstract String prepareSql(Connection connection, Table table, Column column, String whereClauseCondition);

    private String handleOperator(AssertTest assertion, Column column) {
        if (OperatorEnum.IN.getName().equalsIgnoreCase(assertion.getOperator())) {
            return handleInOperator(assertion, column);
        }
        if (assertion.getOperator().equalsIgnoreCase(OperatorEnum.BETWEEN.getName())) {
            return handleBetweenOperator(assertion, column);
        }
        return castValue(assertion.getValue(), column);
    }

    private String handleInOperator(AssertTest assertion, Column column) {
        if (!(assertion.getValue() instanceof List)) {
            throw new RuntimeException(String.format("Operator %s is not allowed with value %s, " +
                    "it is allowed only with array value", assertion.getOperator(), assertion.getValue()));
        }

        @SuppressWarnings("unchecked")
        String values = ((List<Object>) assertion.getValue())
            .stream()
            .map(o -> castValue(o, column))
            .collect(Collectors.joining(","));

        return String.format("(%s)", values);
    }

    private String handleBetweenOperator(AssertTest assertion, Column column) {
        if (!(assertion.getValue() instanceof List)) {
            throw new RuntimeException(String.format("Operator %s is not allowed with value %s, " +
                    "it is allowed only with array value", assertion.getOperator(), assertion.getValue()));
        }

        List<Object> values = ((List<Object>) assertion.getValue())
            .stream()
            .map(o -> castValue(o, column))
            .collect(Collectors.toList());

        if (values.size() < 2) {
            throw new RuntimeException(String.format("Operator %s is not allowed with value %s, " +
                    "it requires two values", assertion.getOperator(), assertion.getValue()));
        }

        return String.format("%s AND %s", values.get(0), values.get(1));
    }

    abstract String castValue(Object value, Column column);

    private boolean allowedOperatorsForNullValue(AssertTest assertion) {
        return OperatorEnum.EQUAL.getName().equals(assertion.getOperator()) || OperatorEnum.NOT_EQUAL.getName().equals(assertion.getOperator());
    }
}
