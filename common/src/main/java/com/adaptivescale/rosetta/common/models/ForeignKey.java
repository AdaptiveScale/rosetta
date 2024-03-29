package com.adaptivescale.rosetta.common.models;

import java.util.Objects;

public class ForeignKey {
    private String name;
    private String schema;
    private String tableName;
    private String columnName;
    private String deleteRule;

    private String primaryTableSchema;
    private String primaryTableName;
    private String primaryColumnName;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
    }

    public String getPrimaryTableSchema() {
        return primaryTableSchema;
    }

    public void setPrimaryTableSchema(String primaryTableSchema) {
        this.primaryTableSchema = primaryTableSchema;
    }

    public String getPrimaryTableName() {
        return primaryTableName;
    }

    public void setPrimaryTableName(String primaryTableName) {
        this.primaryTableName = primaryTableName;
    }

    public String getPrimaryColumnName() {
        return primaryColumnName;
    }

    public void setPrimaryColumnName(String primaryColumnName) {
        this.primaryColumnName = primaryColumnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKey that = (ForeignKey) o;
        return Objects.equals(name, that.name) && Objects.equals(tableName, that.tableName) && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tableName, columnName);
    }
}
