package com.adaptivescale.rosetta.common.models;

import java.util.List;

public class Column {

    private String name;
    private String label;
    private String description;
    private String typeName;
    private String jdbcDataType;
    private int ordinalPosition;
    private boolean isAutoincrement;
    private boolean isNullable;
    private boolean isPrimaryKey;
    private int primaryKeySequenceId;
    private int columnDisplaySize;
    private int scale;
    private int precision;

    private List<ForeignKey> foreignKeys;

    public Column() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public boolean isAutoincrement() {
        return isAutoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        isAutoincrement = autoincrement;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public int getPrimaryKeySequenceId() {
        return primaryKeySequenceId;
    }

    public void setPrimaryKeySequenceId(int primaryKeySequenceId) {
        this.primaryKeySequenceId = primaryKeySequenceId;
    }

    public String getJdbcDataType() {
        return jdbcDataType;
    }

    public void setJdbcDataType(String jdbcDataType) {
        this.jdbcDataType = jdbcDataType;
    }

    public int getColumnDisplaySize() {
        return columnDisplaySize;
    }

    public void setColumnDisplaySize(int columnDisplaySize) {
        this.columnDisplaySize = columnDisplaySize;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        this.foreignKeys=foreignKeys;
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
