package com.adaptivescale.rosetta.ddl.change.model;

import java.util.Objects;

public class Change<T> {
    //state in model
    private T expected;
    //state right now in database
    private T actual;
    private final Status status;
    private final Type type;

    protected Change(T expected, T actual, Status state, Type type) {
        this.expected = expected;
        this.actual = actual;
        this.status = state;

        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public Type getType() {
        return type;
    }

    public T getExpected() {
        return expected;
    }

    public void setExpected(T expected) {
        this.expected = expected;
    }

    public T getActual() {
        return actual;
    }

    public void setActual(T actual) {
        this.actual = actual;
    }

    public enum Type {
        DATABASE,
        TABLE,
        COLUMN,
        FOREIGN_KEY
    }

    public enum Status {
        ALTER,
        DROP,
        ADD
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change<?> change = (Change<?>) o;
        return Objects.equals(expected, change.expected) && Objects.equals(actual, change.actual) && status == change.status && type == change.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expected, actual, status, type);
    }
}
