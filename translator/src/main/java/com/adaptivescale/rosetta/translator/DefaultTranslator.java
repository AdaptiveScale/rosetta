package com.adaptivescale.rosetta.translator;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.TranslationModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;

public class DefaultTranslator implements Translator<Database, Database> {

    private final String targetDatabaseName;

    private final String sourceDatabaseName;

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
        TranslationMatrix translationMatrix = TranslationMatrix.getInstance();
        TranslationModel translationModel = translationMatrix.findBySourceTypeAndSourceColumnTypeAndTargetType(sourceDatabaseName, column.getTypeName(), targetDatabaseName);

        if (translationModel == null) {
            throw new RuntimeException("There is no match for column name: " + column.getName() + " and type: " + column.getTypeName() + ".");
        }
        //todo find a way to create deep copy (faster way)
        try {
            String s = new ObjectMapper().writeValueAsString(column);
            Column result = new ObjectMapper().readValue(s, Column.class);
            result.setTypeName(translationModel.getTargetColumnType());
            result.setColumnDisplaySize(0);
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
