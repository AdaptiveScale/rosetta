package com.adaptivescale.rosetta.common.models;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Table {

    private String name;
    private String description;
    private String type;
    private String schema;

    private Interleave interleave;

    private List<Index> indices;

    private Collection<Column> columns;

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
