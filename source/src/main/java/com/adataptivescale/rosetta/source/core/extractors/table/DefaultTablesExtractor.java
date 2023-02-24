/*
 *  Copyright 2022 AdaptiveScale
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.models.Interleave;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.TableExtractor;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultTablesExtractor implements TableExtractor<Collection<Table>, Connection, java.sql.Connection> {

    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet resultSet = metaData.getTables(target.getDatabaseName(), target.getSchemaName(), null, ArrayUtils.toArray("TABLE"));

        Collection<Table> tables = new ArrayList<>();

        while (resultSet.next()) {
            if (!target.getTables().isEmpty() &&
                !target.getTables().contains(resultSet.getString("TABLE_NAME"))) continue;
            Table table = map(resultSet);
            tables.add(table);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        List<Interleave> interlevedTables = getInterleavedTables(connection);
        for (Interleave interleave: interlevedTables) {
            Table table = tables.stream().filter(it -> it.getName().equals(interleave.getTableName())).findFirst().orElse(null);
            if (table != null) {
                table.setInterleave(interleave);
            }
        }
        return tables;
    }

    private Table map(ResultSet resultSet) throws SQLException {
        Table table = new Table();
        table.setName(resultSet.getString("TABLE_NAME"));
        table.setType(resultSet.getString("TABLE_TYPE"));
        String tableSchema = resultSet.getString("TABLE_SCHEM");
        if(tableSchema==null) {
            tableSchema = resultSet.getString("TABLE_CAT");
        }
        table.setSchema(tableSchema);
        return table;
    }

    private List<Interleave> getInterleavedTables(java.sql.Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT\n" +
            " TABLE_NAME, PARENT_TABLE_NAME, ON_DELETE_ACTION, table_type, SPANNER_STATE, INTERLEAVE_TYPE\n" +
            "  FROM\n" +
            "    information_schema.tables\n" +
            "  WHERE\n" +
            "    table_schema = '' AND PARENT_TABLE_NAME IS NOT NULL");

        List<Interleave> interleavedTables = new ArrayList<>();

        while (resultSet.next()) {
            Interleave interleave = new Interleave();
            interleave.setTableName(resultSet.getString("TABLE_NAME"));
            interleave.setParentName(resultSet.getString("PARENT_TABLE_NAME"));
            interleave.setOnDeleteAction(resultSet.getString("ON_DELETE_ACTION"));
            interleavedTables.add(interleave);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return interleavedTables;
    }

}
