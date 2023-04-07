package com.adaptivescale.rosetta.common.models;

import java.util.ArrayList;
import java.util.List;

public class TranslationModel {
    private Integer id;
    private String sourceType;
    private String sourceColumnType;
    private String targetType;
    private String targetColumnType;
    private List<TranslationAttributeModel> attributes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceColumnType() {
        return sourceColumnType;
    }

    public void setSourceColumnType(String sourceColumnType) {
        this.sourceColumnType = sourceColumnType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetColumnType() {
        return targetColumnType;
    }

    public void setTargetColumnType(String targetColumnType) {
        this.targetColumnType = targetColumnType;
    }

    public List<TranslationAttributeModel> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<TranslationAttributeModel> attributes) {
        this.attributes = attributes;
    }

    public String generateInsertStatement(String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(tableName)
                .append(" values (")
                .append(id)
                .append(", '").append(sourceType).append("' ")
                .append(", '").append(sourceColumnType).append("' ")
                .append(", '").append(targetType).append("' ")
                .append(", '").append(targetColumnType).append("' ")
                .append(");");
        return builder.toString();
    }

    @Override
    public String toString() {
        return "TranslationModel{" +
                "id=" + id +
                ", sourceType='" + sourceType + '\'' +
                ", sourceColumnType='" + sourceColumnType + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetColumnType='" + targetColumnType + '\'' +
                '}';
    }
}
