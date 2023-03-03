package com.adaptivescale.rosetta.common.models;

import java.util.Objects;

public class Interleave {

    private String tableName;

    private String parentName;

    private String onDeleteAction;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getOnDeleteAction() {
        return onDeleteAction;
    }

    public void setOnDeleteAction(String onDeleteAction) {
        this.onDeleteAction = onDeleteAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interleave that = (Interleave) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(parentName, that.parentName) && Objects.equals(onDeleteAction, that.onDeleteAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, parentName, onDeleteAction);
    }

}
