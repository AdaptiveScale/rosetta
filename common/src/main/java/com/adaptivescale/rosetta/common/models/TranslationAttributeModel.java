package com.adaptivescale.rosetta.common.models;

public class TranslationAttributeModel {

    private Integer id;
    private Integer translationId;
    private String attributeName;
    private String attributeValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTranslationId() {
        return translationId;
    }

    public void setTranslationId(Integer translationId) {
        this.translationId = translationId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String generateInsertStatement(String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(tableName)
            .append(" values (")
            .append(id)
            .append(", '").append(translationId).append("' ")
            .append(", '").append(attributeName).append("' ")
            .append(", '").append(attributeValue).append("' ")
            .append(");");
        return builder.toString();
    }

    @Override
    public String toString() {
        return "TranslationAttributeModel{" +
                "id=" + id +
                ", translationId=" + translationId +
                ", attributeName='" + attributeName + '\'' +
                ", attributeValue='" + attributeValue + '\'' +
                '}';
    }
}
