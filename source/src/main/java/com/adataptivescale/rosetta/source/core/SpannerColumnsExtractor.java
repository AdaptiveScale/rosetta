package com.adataptivescale.rosetta.source.core;

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
    column.setTypeName(String.valueOf(resultSet.getString("TYPE_NAME")));
    column.setNullable(resultSet.getString("IS_NULLABLE").equals("YES"));
    column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
    column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
    column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
  }
}
