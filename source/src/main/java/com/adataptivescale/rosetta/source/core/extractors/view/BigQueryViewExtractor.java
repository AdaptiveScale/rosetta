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

package com.adataptivescale.rosetta.source.core.extractors.view;

import com.adaptivescale.rosetta.common.models.View;
import com.adataptivescale.rosetta.source.common.QueryHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class BigQueryViewExtractor extends DefaultViewExtractor{
    @Override
    protected void attachViewDDL(Collection<View> views, java.sql.Connection connection) throws SQLException {
        HashMap<String, List<View>> viewsBySchema = new HashMap<>();
        for (View view : views) {
            viewsBySchema.computeIfAbsent(view.getSchema(), k->new ArrayList<View>()).add(view);
        }
        for (String schemaName : viewsBySchema.keySet()) {
            Statement statement = connection.createStatement();
            String query = String.format("select * from %s.INFORMATION_SCHEMA.TABLES where table_type='VIEW'", schemaName);
            ResultSet resultSet = statement.executeQuery(query);
            List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);
            for (Map<String, Object> record : records) {
                Optional<View> tmpTable = viewsBySchema.get(schemaName).stream()
                        .filter(view -> view.getName().equals(record.get("table_name"))).findAny();
                String[] ddls = record.get("ddl").toString().split("\n");
                String ddl = Arrays.stream(Arrays.copyOfRange(ddls, 1, ddls.length)).collect(Collectors.joining(" "));
                if(ddl.endsWith(";")) {
                    ddl= ddl.substring(0, ddl.length()-2);
                }
                String finalDdl = ddl;
                tmpTable.ifPresent(table -> table.setCode(finalDdl));
            }
        }
    }
}
