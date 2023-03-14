package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Interleave;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RosettaModule(
        name = "spanner",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)
public class SpannerTablesExtractor extends DefaultTablesExtractor {
    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet resultSet = metaData.getTables(target.getDatabaseName(), target.getSchemaName(), null, ArrayUtils.toArray("TABLE"));

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
        List<Interleave> interlevedTables = getInterleavedTables(connection);
        for (Interleave interleave: interlevedTables) {
            Table table = tables.stream().filter(it -> it.getName().equals(interleave.getTableName())).findFirst().orElse(null);
            if (table != null) {
                table.setInterleave(interleave);
            }
        }
        return tables;
    }

    private List<Interleave> getInterleavedTables(java.sql.Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT\n" +
                " TABLE_NAME, PARENT_TABLE_NAME, ON_DELETE_ACTION, table_type, SPANNER_STATE, INTERLEAVE_TYPE\n" +
                "  FROM\n" +
                "    information_schema.tables\n" +
                "  WHERE\n" +
                "    table_schema = '' AND PARENT_TABLE_NAME IS NOT NULL");

        List<Interleave> interleavedTables = new ArrayList<>();

        while (resultSet.next()) {
            Interleave interleave = new Interleave();
            interleave.setTableName(resultSet.getString("TABLE_NAME"));
            interleave.setParentName(resultSet.getString("PARENT_TABLE_NAME"));
            interleave.setOnDeleteAction(resultSet.getString("ON_DELETE_ACTION"));
            interleavedTables.add(interleave);
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return interleavedTables;
    }
}
