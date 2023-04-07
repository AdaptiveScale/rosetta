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

package com.adataptivescale.rosetta.source.core.extractors.column;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;

@RosettaModule(
        name = "spanner",
        type = RosettaModuleTypes.COLUMN_EXTRACTOR
)
public class SpannerColumnsExtractor extends ColumnsExtractor {
  public SpannerColumnsExtractor(Connection connection) {
    super(connection);
  }

  @Override
  protected void extract(ResultSet resultSet, Column column) throws SQLException {
    column.setName(resultSet.getString("COLUMN_NAME"));

    String columnType = String.valueOf(resultSet.getString("TYPE_NAME"));
    column.setTypeName(TranslationMatrix.getInstance().findBySourceTypeAndSourceColumnType("spanner", columnType));

    column.setNullable(resultSet.getString("IS_NULLABLE").equals("YES"));
    column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
    column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
    column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
  }
}
