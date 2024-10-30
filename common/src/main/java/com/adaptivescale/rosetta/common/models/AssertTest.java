package com.adaptivescale.rosetta.common.models;

import com.adaptivescale.rosetta.common.models.test.Test;

public class AssertTest implements Test {

    private String operator;
    private Object value;
    private String expected;
    private String columnDef;

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

    public String getColumnDef() {
        return columnDef;
    }

    public void setColumnDef(String columnDef) {
        this.columnDef = columnDef;
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
