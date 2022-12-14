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
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.core.interfaces.ViewExtractor;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultViewExtractor implements ViewExtractor<Collection<View>, Connection, java.sql.Connection> {
    @Override
    public Collection<View> extract(Connection target, java.sql.Connection connection) throws Exception {
        Collection<View> views = extractViews(target, connection);
        attachViewDDL(views, connection);
        return views;
    }

    private Collection<View> extractViews(Connection target, java.sql.Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet resultSet = metaData.getTables(target.getDatabaseName(), target.getSchemaName(), null, ArrayUtils.toArray("VIEW"));

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

    protected void attachViewDDL(Collection<View> views, java.sql.Connection connection) throws SQLException {
        // No op
    }

    private View map(ResultSet resultSet) throws SQLException {
        View view = new View();
        view.setName(resultSet.getString("TABLE_NAME"));
        view.setType(resultSet.getString("TABLE_TYPE"));
        String tableSchema = resultSet.getString("TABLE_SCHEM");
        if(tableSchema==null) {
            tableSchema = resultSet.getString("TABLE_CAT");
        }
        view.setSchema(tableSchema);
        return view;
    }
}
