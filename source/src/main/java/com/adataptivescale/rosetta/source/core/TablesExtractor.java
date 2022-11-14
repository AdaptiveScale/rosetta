package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TablesExtractor implements TableExtractor<Collection<Table>, Connection, java.sql.Connection> {
    private String[] EXTRACT_TYPES = new String[]{"TABLE"};
    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(target.getDatabaseName(), target.getSchemaName(), null, getEXTRACT_TYPES());

        Collection<Table> tables = new ArrayList<>();

        while (resultSet.next()) {
            if (!target.getTables().isEmpty() &&
                !target.getTables().contains(resultSet.getString("TABLE_NAME"))) continue;

            Table table = new Table();
            table.setName(resultSet.getString("TABLE_NAME"));
            table.setType(resultSet.getString("TABLE_TYPE"));
            String tableSchema = resultSet.getString("TABLE_SCHEM");
            if(tableSchema==null) {
                tableSchema = resultSet.getString("TABLE_CAT");
            }
            table.setSchema(tableSchema);

            tables.add(table);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return tables;
    }

    public String[] getEXTRACT_TYPES() {
        return EXTRACT_TYPES;
    }

    public void setEXTRACT_TYPES(String[] EXTRACT_TYPES) {
        this.EXTRACT_TYPES = EXTRACT_TYPES;
    }
}
