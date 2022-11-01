package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Index;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ColumnsExtractor implements ColumnExtractor<java.sql.Connection, Collection<Table>> {

    private final Connection connection;

    public ColumnsExtractor(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void extract(java.sql.Connection connection, Collection<Table> tables) throws Exception {
        for (Table table : tables) {
            Collection<Column> columns = new ArrayList<>();
            Map<String, Integer> primaryKeysData = extractPrimaryKeys(connection, table);
            Map<String, List<ForeignKey>> foreignKeys = extractForeignKeys(connection, table);
            Map<String, List<Index>> indices = extractIndices(connection, table);
            ResultSet resultSet = connection.getMetaData().getColumns(this.connection.getDatabaseName(), table.getSchema(), table.getName(), null);

            while (resultSet.next()) {
                Column column = new Column();
                extract(resultSet, column);
                if (primaryKeysData.containsKey(column.getName())) {
                    column.setPrimaryKey(true);
                    column.setPrimaryKeySequenceId(primaryKeysData.get(column.getName()));
                }
                if (foreignKeys.containsKey(column.getName())) {
                    column.setForeignKeys(foreignKeys.get(column.getName()));
                }
                columns.add(column);
                table.setColumns(columns);
            }

            if (indices.containsKey(table.getName())) {
                table.setIndices(indices.get(table.getName()));
            }

            if (!resultSet.isClosed()) {
                resultSet.close();
            }
        }
    }

    protected void extract(ResultSet resultSet, Column column) throws SQLException {
        column.setName(resultSet.getString("COLUMN_NAME"));
        column.setTypeName(String.valueOf(resultSet.getString("TYPE_NAME")));
        column.setNullable(resultSet.getBoolean("IS_NULLABLE"));
        column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
        column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
        column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
    }

    private Map<String, List<ForeignKey>> extractForeignKeys(java.sql.Connection connection, Table table) throws SQLException {
        ResultSet exportedKeys = connection.getMetaData().getImportedKeys(this.connection.getDatabaseName(), table.getSchema(), table.getName());
        Map<String, List<ForeignKey>> result = new HashMap<>();

        while (exportedKeys.next()) {
            ForeignKey foreignKey = new ForeignKey();
            foreignKey.setName(exportedKeys.getString("FK_NAME"));
            foreignKey.setSchema(exportedKeys.getString("FKTABLE_SCHEM"));
            foreignKey.setTableName(exportedKeys.getString("FKTABLE_NAME"));
            foreignKey.setColumnName(exportedKeys.getString("FKCOLUMN_NAME"));
            foreignKey.setDeleteRule(exportedKeys.getString("DELETE_RULE"));

            foreignKey.setPrimaryTableSchema(exportedKeys.getString("PKTABLE_SCHEM"));
            foreignKey.setPrimaryTableName(exportedKeys.getString("PKTABLE_NAME"));
            foreignKey.setPrimaryColumnName(exportedKeys.getString("PKCOLUMN_NAME"));

            List<ForeignKey> foreignKeys = result.computeIfAbsent(foreignKey.getColumnName(), k -> new ArrayList<>());
            foreignKeys.add(foreignKey);
        }

        return result;
    }

    private Map<String, Integer> extractPrimaryKeys(java.sql.Connection connection, Table table) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(this.connection.getDatabaseName(), table.getSchema(), table.getName());
        while (primaryKeys.next()) {
            result.put(primaryKeys.getString("COLUMN_NAME"),
                    primaryKeys.getInt("KEY_SEQ"));
        }
        return result;
    }

    private Map<String, List<Index>> extractIndices(java.sql.Connection connection, Table table) throws SQLException {
        ResultSet exportedKeys = connection.getMetaData().getIndexInfo(this.connection.getDatabaseName(), table.getSchema(), table.getName(), false, false);
        Map<String, List<Index>> result = new HashMap<>();
        Map<String, Index> indicesMappedByName = new HashMap<>();

        while (exportedKeys.next()) {
            String indexName = exportedKeys.getString("INDEX_NAME");
            Index index = indicesMappedByName.getOrDefault(indexName, new Index());

            index.setName(exportedKeys.getString("INDEX_NAME"));
            index.setSchema(exportedKeys.getString("TABLE_SCHEM"));
            index.setTableName(exportedKeys.getString("TABLE_NAME"));
            index.addColumn(exportedKeys.getString("COLUMN_NAME"));
            index.setIndexQualifier(exportedKeys.getString("INDEX_QUALIFIER"));
            index.setType(exportedKeys.getShort("TYPE"));
            index.setCardinality(exportedKeys.getInt("CARDINALITY"));
            index.setFilterCondition(exportedKeys.getString("FILTER_CONDITION"));
            index.setNonUnique(exportedKeys.getBoolean("NON_UNIQUE"));
            index.setAscOrDesc(exportedKeys.getString("ASC_OR_DESC"));

            indicesMappedByName.put(indexName, index);
//            List<Index> indices = result.computeIfAbsent(table.getName(), k -> new ArrayList<>());
//            indices.add(index);
        }

        result.put(table.getName(), new ArrayList<Index>(indicesMappedByName.values()));
        return result;
    }
}
