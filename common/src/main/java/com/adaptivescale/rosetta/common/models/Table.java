package com.adaptivescale.rosetta.common.models;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Table {

    private String name;
    private String description;
    private String type;
    private String schema;

    private Interleave interleave;

    private List<Index> indices;

    private Collection<Column> columns;

    private String extract;

    private String load;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Collection<Column> getColumns() {
        return columns;
    }

    public void setColumns(Collection<Column> columns) {
        this.columns = columns;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Interleave getInterleave() {
        return interleave;
    }

    public void setInterleave(Interleave interleave) {
        this.interleave = interleave;
    }

    public List<Index> getIndices() {
        return indices;
    }

    public void setIndices(List<Index> indices) {
        this.indices = indices;
    }

    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }

    public String getLoad() {
        return load;
    }

    public void setLoad(String load) {
        this.load = load;
    }

    public void generateExtractSql() {
        final StringBuilder extractSql = new StringBuilder();
        List<String> columnNames = this.columns
            .stream()
            .map(Column::getName)
            .collect(Collectors.toList());
        extractSql.append("SELECT " + String.join(", ", columnNames) + " FROM " + this.name + ";");
        this.extract = extractSql.toString();
    }

    public void generateLoadSql() {
        final StringBuilder loadSql = new StringBuilder();
        List<String> columnNames = this.columns
            .stream()
            .map(Column::getName)
            .collect(Collectors.toList());
        loadSql.append("INSERT INTO " + this.name + " (" + String.join(", ", columnNames) + ")");

        List<String> columnNameTemplates = this.columns
            .stream().map(Column::getName)
            .map(it -> String.format("'[(${%s})]'", it))
            .collect(Collectors.toList());
        loadSql.append(" VALUES (" + String.join(", ", columnNameTemplates) + ")");
        this.load = loadSql.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(name, table.name) && Objects.equals(description, table.description) && Objects.equals(type, table.type) && Objects.equals(schema, table.schema) && Objects.equals(interleave, table.interleave) && Objects.equals(indices, table.indices) && Objects.equals(columns, table.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, type, schema, interleave, indices, columns);
    }
}
