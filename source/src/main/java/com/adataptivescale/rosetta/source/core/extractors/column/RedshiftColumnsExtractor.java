package com.adataptivescale.rosetta.source.core.extractors.column;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;


@RosettaModule(
        name = "redshift",
        type = RosettaModuleTypes.COLUMN_EXTRACTOR
)
public class RedshiftColumnsExtractor extends ColumnsExtractor {

    public RedshiftColumnsExtractor(Connection connection) {
        super(connection);
    }

    @Override
    protected void extract(ResultSet resultSet, Column column) throws SQLException {
        column.setName(resultSet.getString("COLUMN_NAME"));

        String columnType = String.valueOf(resultSet.getString("TYPE_NAME"));
        column.setTypeName(TranslationMatrix.getInstance().findBySourceTypeAndSourceColumnType("redshift", columnType));

        column.setNullable(resultSet.getBoolean("IS_NULLABLE"));
        column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
        column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
        column.setPrecision(resultSet.getInt("COLUMN_SIZE"));
    }

}
