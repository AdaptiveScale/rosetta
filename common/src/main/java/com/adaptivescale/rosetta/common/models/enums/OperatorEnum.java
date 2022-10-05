package com.adaptivescale.rosetta.common.models.enums;

public enum OperatorEnum {

    EQUAL("=", "equal"),
    NOT_EQUAL("!=", "not_equal"),
    BIGGER(">", "bigger"),
    LOWER("<", "lower"),
    BIGGER_OR_EQUAL(">=", "bigger or equal"),
    LOWER_OR_EQUAL("<=", "lower or equal"),
    IN("in", "in"),
    IS_NULL("is null", "is null"),
    IS_NOT_NULL("is not null", "is not null"),
    BETWEEN("between", "between"),
    LIKE("like", "like"),
    UNIQUE("unique", "unique");

    private String operator;
    private String name;

    OperatorEnum(String operator, String name) {
        this.operator = operator;
        this.name = name;
    }

    public String getOperator() {
        return operator;
    }

    public String getName() {
        return name;
    }
}