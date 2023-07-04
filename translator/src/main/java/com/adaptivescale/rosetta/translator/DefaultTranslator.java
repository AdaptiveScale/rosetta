package com.adaptivescale.rosetta.translator;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.common.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultTranslator implements Translator<Database, Database> {

    private final String sourceDatabaseName;
    private final String targetDatabaseName;

    public DefaultTranslator(String sourceDatabaseName, String targetDatabaseName) {
        this.sourceDatabaseName = sourceDatabaseName;
        this.targetDatabaseName = targetDatabaseName;
    }

    @Override
    public Database translate(Database input) {
        Database result = new Database();
        result.setName(input.getDatabaseProductName());
        result.setTables(input.getTables().stream().map(this::translateTable).collect(Collectors.toList()));
        result.setDatabaseType(targetDatabaseName);
        return result;
    }

    private Table translateTable(Table table) {
        Table newTable = new Table();
        newTable.setName(table.getName());
        newTable.setType(table.getType());
        newTable.setSchema(table.getSchema());
        newTable.setColumns(table
            .getColumns()
            .stream()
            .map(this::translateColumn)
            .collect(Collectors.toList()));
        return newTable;
    }


    private Column translateColumn(Column column) {
        String columnType = column.getOverwriteType();
        if (columnType == null) {
            columnType = column.getTypeName();
        }
        TranslationModel translationModel = TranslationMatrix.getInstance().findBySourceTypeAndSourceColumnTypeAndTargetType(sourceDatabaseName, columnType, targetDatabaseName);

        if (translationModel == null) {
            throw new RuntimeException("There is no match for column name: " + column.getName() + " and type: " + column.getTypeName() + ".");
        }

        List<TranslationAttributeModel> translationAttributes = TranslationMatrix.getInstance().findByTranslationAttributesByTranslationIds(translationModel.getId());
        translationModel.setAttributes(translationAttributes);

        try {
            String s = new ObjectMapper().writeValueAsString(column);
            Column result = new ObjectMapper().readValue(s, Column.class);
            result.setTypeName(translationModel.getTargetColumnType());

            for (TranslationAttributeModel attribute : translationModel.getAttributes()) {
                String value = attribute.getAttributeValue();
                switch(attribute.getAttributeName()) {
                    case "ordinalPosition":
                        result.setOrdinalPosition(Integer.valueOf(value));
                        break;
                    case "autoincrement":
                        result.setAutoincrement(Boolean.valueOf(value));
                        break;
                    case "nullable":
                        result.setNullable(Boolean.valueOf(value));
                        break;
                    case "primaryKey":
                        result.setPrimaryKey(Boolean.valueOf(value));
                        break;
                    case "primaryKeySequenceId":
                        result.setPrimaryKeySequenceId(Integer.valueOf(value));
                        break;
                    case "columnDisplaySize":
                        result.setColumnDisplaySize(Integer.valueOf(value));
                        break;
                    case "scale":
                        result.setScale(Integer.valueOf(value));
                        break;
                    case "precision":
                        result.setPrecision(Integer.valueOf(value));
                        break;
                }
            }

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
