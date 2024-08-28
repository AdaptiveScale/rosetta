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
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.View;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RosettaModule(name = "kinetica", type = RosettaModuleTypes.VIEW_EXTRACTOR)
public class KineticaViewExtractor extends DefaultViewExtractor {
    @Override
    protected void attachViewDDL(Collection<View> views, java.sql.Connection connection) {
        if (views.isEmpty()) {
            return;
        }

        List<String> schemaList = views.stream()
                .map(Table::getSchema)
                .map(schema -> "'" + schema + "'") // Wrap each string in single quotes
                .collect(Collectors.toList());

        // Fetch view info only (M is for Materialized, V is Logical view)
        String queryTemplate = "SELECT object_name, schema_name, shard_kind, persistence, obj_kind, definition " +
                "FROM ki_catalog.ki_objects " +
                "WHERE schema_name IN (%s) AND obj_kind in ('V', 'M')";

        try (Statement statement = connection.createStatement()) {
            String query = String.format(queryTemplate, String.join(", ", schemaList));
            ResultSet resultSet = statement.executeQuery(query);
            List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);

            for (View view : views) {
                Optional<Map<String, Object>> record = records.stream().filter(rec -> view.getName().equals(rec.get("object_name")) && view.getSchema().equals(rec.get("schema_name"))).findAny();

                if (record.isPresent()) {
                    view.setCode(record.get().get("definition").toString());
                    if (record.get().get("obj_kind").equals("M")) {
                        view.setMaterialized(true);
                    }
                }
            }

        } catch (SQLException e) {
            log.warn("Skipping processing views due to error: {}", e.getMessage());
        }

        // Skip view due to incomplete code, safety issue
        views.removeIf(view -> {
            if (view.getCode() == null || view.getCode().isEmpty() || view.getCode().length() == 256) {
                log.warn("Skipping view due to incomplete code: {}.{}", view.getSchema(), view.getName());
                return true;
            }
            return false;
        });
    }
}
