package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adataptivescale.rosetta.source.core.interfaces.ColumnExtractor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BigQueryColumnsExtractor extends ColumnsExtractor {

    public BigQueryColumnsExtractor(Target target) {
        super(target);
    }

    // is_autoincrement => is returned true always by bigquery jdbc driver
    @Override
    protected void extract(ResultSet resultSet, Column column) throws SQLException {
        column.setName(resultSet.getString("column_name".toUpperCase()));
        column.setJdbcDataType(String.valueOf(resultSet.getInt("data_type".toUpperCase())));
        column.setTypeName(String.valueOf(resultSet.getString("type_name".toUpperCase())));
        column.setNullable(resultSet.getBoolean("is_nullable".toUpperCase()));
        column.setColumnDisplaySize(resultSet.getInt("column_size".toUpperCase()));
    }

}
