package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class TablesExtractor implements TableExtractor<Collection<Table>, Target, Connection> {
    @Override
    public Collection<Table> extract(Target target, Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, target.getSchemaName(), null, new String[]{"TABLE"});

        Collection<Table> tables = new ArrayList<>();

        while (resultSet.next()) {
            Table table = new Table();
            table.setName(resultSet.getString("TABLE_NAME"));
            table.setType(resultSet.getString("TABLE_TYPE"));
            table.setSchema(resultSet.getString("TABLE_SCHEM"));

            tables.add(table);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return tables;
    }
}
