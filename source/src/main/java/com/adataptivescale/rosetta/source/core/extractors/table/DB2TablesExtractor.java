package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

@RosettaModule(
        name = "db2",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)
public class DB2TablesExtractor extends DefaultTablesExtractor {

    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet resultSet = metaData.getTables(null, target.getSchemaName(), null, ArrayUtils.toArray("TABLE"));

        Collection<Table> tables = new ArrayList<>();

        while (resultSet.next()) {
            if (!target.getTables().isEmpty() &&
                    !target.getTables().contains(resultSet.getString("TABLE_NAME"))) continue;
            Table table = map(resultSet);
            tables.add(table);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }

        return tables;
    }
}
