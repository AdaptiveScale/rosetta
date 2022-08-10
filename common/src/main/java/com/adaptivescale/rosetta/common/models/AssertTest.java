package com.adaptivescale.rosetta.common.models;

import com.adaptivescale.rosetta.common.models.test.Test;

public class AssertTest implements Test {
    public final static String EQUAL = "=";
    public final static String NOT_EQUAL = "!=";
    public final static String BIGGER = ">";
    public final static String LOWER = ">";
    public final static String BIGGER_EQUAL = ">=";
    public final static String LOWER_EQUAL = "<=";
    public final static String IN = "in";

    private String operator;
    private Object value;
    private String expected;

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    @Override
    public String toString() {
        return "AssertTest{" +
                "operator='" + operator + '\'' +
                ", value='" + value + '\'' +
                ", expected='" + expected + '\'' +
                '}';
    }
}
