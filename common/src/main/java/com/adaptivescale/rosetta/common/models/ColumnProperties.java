package com.adaptivescale.rosetta.common.models;

public class ColumnProperties {

    private String name;

    private Integer sequenceId;

    public ColumnProperties() {
    }

    public ColumnProperties(String name, Integer sequenceId) {
        this.name = name;
        this.sequenceId = sequenceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "ColumnProperties{" +
                "name='" + name + '\'' +
                ", sequenceId=" + sequenceId +
                '}';
    }
}
