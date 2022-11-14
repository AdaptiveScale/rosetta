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

package com.adataptivescale.rosetta.source.core.extractors;

import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import com.adataptivescale.rosetta.source.core.TablesExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class BigQueryTableExtractor extends TablesExtractor {

    public final String[] EXTRACT_TYPES = new String[]{"TABLE", "VIEW"};

    public BigQueryTableExtractor(){
        super.setEXTRACT_TYPES(EXTRACT_TYPES);
    }

    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        Collection<Table> tables = super.extract(target, connection);
        attachViewDDLs(tables, connection);
        return tables;
    }

    private void attachViewDDLs(Collection<Table> tables, java.sql.Connection connection) throws SQLException {
        List<Table> views = tables.stream().filter(table -> table.getType().equals("VIEW")).collect(Collectors.toList());
        HashMap<String, List<Table>> viewsBySchema = new HashMap<>();
        for (Table view : views) {
            viewsBySchema.computeIfAbsent(view.getSchema(), k->new ArrayList<Table>()).add(view);
        }
        for (String schemaName : viewsBySchema.keySet()) {
            Statement statement = connection.createStatement();
            String query = String.format("select * from %s.INFORMATION_SCHEMA.TABLES where table_type='VIEW'", schemaName);
            ResultSet resultSet = statement.executeQuery(query);
            List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);
            for (Map<String, Object> record : records) {
                Optional<Table> tmpTable = viewsBySchema.get(schemaName).stream()
                        .filter(view -> view.getName().equals(record.get("table_name"))).findAny();
                String ddl = record.get("ddl").toString();
                tmpTable.ifPresent(table -> table.setCode(ddl));
            }
        }
    }

}
