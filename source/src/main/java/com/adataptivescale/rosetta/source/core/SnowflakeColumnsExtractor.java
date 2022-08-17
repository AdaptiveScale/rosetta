package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.input.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SnowflakeColumnsExtractor extends ColumnsExtractor {
  public SnowflakeColumnsExtractor(Connection connection) {
    super(connection);
  }

  // is_nullable => is returned "YES" for true and "NO" for false instead of boolean by snowflake jdbc driver
  @Override
  protected void extract(ResultSet resultSet, Column column) throws SQLException {
    column.setName(resultSet.getString("COLUMN_NAME"));
    column.setTypeName(String.valueOf(resultSet.getString("TYPE_NAME")));
    column.setNullable("YES".equals(resultSet.getObject("IS_NULLABLE")));
    column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
    column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
    column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
  }
}
