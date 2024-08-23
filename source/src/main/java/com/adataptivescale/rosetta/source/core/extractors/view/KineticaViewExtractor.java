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

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adataptivescale.rosetta.source.common.QueryHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.VIEW_EXTRACTOR
)
public class KineticaViewExtractor extends DefaultViewExtractor{
    @Override
    protected void attachViewDDL(Collection<View> views, java.sql.Connection connection) throws SQLException {
        HashMap<String, List<View>> viewsBySchema = new HashMap<>();
        for (View view : views) {
            viewsBySchema.computeIfAbsent(view.getSchema(), k->new ArrayList<View>()).add(view);
        }

        //Logical Views
        processViews(connection, viewsBySchema,
                "select * from information_schema.VIEWS where TABLE_SCHEMA='%s'",
                "table_name",
                "view_definition",
                false);

        //Materialized views
        processViews(connection, viewsBySchema,
                "select * from pg_catalog.pg_matviews where schemaname='%s'",
                "matviewname",
                "definition",
                true);
    }

    private void processViews(java.sql.Connection connection, HashMap<String, List<View>> viewsBySchema,
                              String queryTemplate, String nameField, String ddlField,
                              boolean isMaterialized) throws SQLException {
        for (String schemaName : viewsBySchema.keySet()) {
            Statement statement = connection.createStatement();
            String query = String.format(queryTemplate, schemaName);
            ResultSet resultSet = statement.executeQuery(query);
            List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);
            for (Map<String, Object> record : records) {
                Optional<View> tmpTable = viewsBySchema.get(schemaName).stream()
                        .filter(view -> view.getName().equals(record.get(nameField))).findAny();
                if (tmpTable.isPresent()) {
                    String finalDdl = processDDL(record.get(ddlField).toString());
                    tmpTable.get().setCode(finalDdl);
                    if (isMaterialized) {
                        tmpTable.get().setMaterialized(true);
                    }
                }
            }
        }
    }

    private String processDDL(String ddl) {
        String[] ddls = ddl.split("\n");
        String processedDdl = Arrays.stream(Arrays.copyOfRange(ddls, 1, ddls.length))
                .collect(Collectors.joining(" "));
        if (processedDdl.endsWith(";")) {
            processedDdl = processedDdl.substring(0, processedDdl.length() - 2);
        }
        return processedDdl.replaceAll("\\s+", " ").trim();
    }
}
