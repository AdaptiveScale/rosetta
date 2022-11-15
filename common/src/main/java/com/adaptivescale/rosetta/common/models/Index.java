package com.adaptivescale.rosetta.common.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Index {

    private String name;
    private String schema;
    private String tableName;
    private List<String> columnNames = new ArrayList<>();
    private Boolean nonUnique;
    private String indexQualifier;
    private Short type;
    private String ascOrDesc;
    private Integer cardinality;
    private String filterCondition;

    public Index() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void addColumn(String columnName) {
        this.columnNames.add(columnName);
    }

    public Boolean getNonUnique() {
        return nonUnique;
    }

    public void setNonUnique(Boolean nonUnique) {
        this.nonUnique = nonUnique;
    }

    public String getIndexQualifier() {
        return indexQualifier;
    }

    public void setIndexQualifier(String indexQualifier) {
        this.indexQualifier = indexQualifier;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public String getAscOrDesc() {
        return ascOrDesc;
    }

    public void setAscOrDesc(String ascOrDesc) {
        this.ascOrDesc = ascOrDesc;
    }

    public Integer getCardinality() {
        return cardinality;
    }

    public void setCardinality(Integer cardinality) {
        this.cardinality = cardinality;
    }

    public String getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index index = (Index) o;
        return Objects.equals(name, index.name) && Objects.equals(schema, index.schema) && Objects.equals(tableName, index.tableName) && Objects.equals(columnNames, index.columnNames) && Objects.equals(nonUnique, index.nonUnique) && Objects.equals(indexQualifier, index.indexQualifier) && Objects.equals(type, index.type) && Objects.equals(ascOrDesc, index.ascOrDesc) && Objects.equals(cardinality, index.cardinality) && Objects.equals(filterCondition, index.filterCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema, tableName, columnNames, nonUnique, indexQualifier, type, ascOrDesc, cardinality, filterCondition);
    }
}
