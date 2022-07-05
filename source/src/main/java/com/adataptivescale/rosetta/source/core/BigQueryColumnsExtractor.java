package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.input.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigQueryColumnsExtractor extends ColumnsExtractor {

    public BigQueryColumnsExtractor(Connection connection) {
        super(connection);
    }

    // is_autoincrement => is returned true always by bigquery jdbc driver
    @Override
    protected void extract(ResultSet resultSet, Column column) throws SQLException {
        column.setName(resultSet.getString("COLUMN_NAME"));
        column.setTypeName(String.valueOf(resultSet.getString("TYPE_NAME")));
        column.setNullable(resultSet.getBoolean("IS_NULLABLE"));
        column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
        column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
        column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
    }

}
