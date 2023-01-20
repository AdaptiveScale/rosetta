package com.adaptivescale.rosetta.translator;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.translator.model.ConvertType;
import com.adaptivescale.rosetta.translator.model.TranslateInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultTranslator implements Translator<Database, Database> {

//    private final TranslateInfo translateInfo;
    private final String targetDatabaseName;

    private final String sourceDatabaseName;

//    public DefaultTranslator(TranslateInfo translateInfo, String targetDatabaseName) {
//        this.translateInfo = translateInfo;
//        this.targetDatabaseName = targetDatabaseName;
//    }

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
//        String sourceName = column.getTypeName();
        TranslationMatrix.TranslationModel translationModel = TranslationMatrix.getInstance().get(sourceDatabaseName, column.getTypeName(), targetDatabaseName);
        //find in which target this source name is there
//        Optional<ConvertType> match = translateInfo.getConverters().stream()
//                .filter(convertType ->
//                        convertType.getCompatibleTypes()
//                                .stream()
//                                .anyMatch(compatibleType
//                                        -> compatibleType.getTypeName()
//                                        .equalsIgnoreCase(sourceName)))
//                .findFirst();

        if (translationModel == null) {
            throw new RuntimeException("There is no match for column name: " + column.getName() + " and type: " + column.getTypeName() + ".");
        }
        //todo find a way to create deep copy (faster way)
        try {
            String s = new ObjectMapper().writeValueAsString(column);
            Column result = new ObjectMapper().readValue(s, Column.class);
            result.setTypeName(translationModel.getTargetColumnType());
            result.setColumnDisplaySize(0);
//            result.setTypeName(match.get().getTargetTypeName());
//            result.setColumnDisplaySize(match.get().getLength());
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
