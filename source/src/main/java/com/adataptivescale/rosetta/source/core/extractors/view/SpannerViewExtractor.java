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
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adataptivescale.rosetta.source.common.QueryHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RosettaModule(
        name = "spanner",
        type = RosettaModuleTypes.VIEW_EXTRACTOR
)
public class SpannerViewExtractor extends DefaultViewExtractor {
    @Override
    protected Collection<View> extractViews(Connection target, java.sql.Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT 'VIEW' AS TABLE_TYPE, TABLE_SCHEMA as TABLE_SCHEM, * FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA=''");

        Collection<View> views = new ArrayList<>();

        while (resultSet.next()) {
            if (!target.getTables().isEmpty() &&
              !target.getTables().contains(resultSet.getString("TABLE_NAME"))) continue;
            View view = map(resultSet);
            views.add(view);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return views;
    }

    @Override
    protected void attachViewDDL(Collection<View> views, java.sql.Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        String query = "select * from INFORMATION_SCHEMA.VIEWS";
        ResultSet resultSet = statement.executeQuery(query);
        List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);
        for (Map<String, Object> record : records) {
            String ddl = record.get("view_definition").toString();
            Optional<View> optionalView = views.stream().filter(tmp -> tmp.getName().equals(record.get("table_name"))).findAny();
            optionalView.ifPresent(view -> view.setCode(ddl));
        }
    }
}
