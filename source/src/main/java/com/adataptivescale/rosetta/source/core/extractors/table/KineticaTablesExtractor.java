package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)
public class KineticaTablesExtractor extends DefaultTablesExtractor {
    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        Collection<Table> tables = super.extract(target, connection);

        return attachTableType(tables, connection);
    }

    private Collection<Table> attachTableType(Collection<Table> tables, java.sql.Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT object_name, schema_name, shard_kind, persistence FROM ki_catalog.ki_objects;");

        while (resultSet.next()) {
            String object_schema = resultSet.getString("schema_name");
            String object_name = resultSet.getString("object_name");
            Optional<Table> found_table = tables.stream().filter(table -> table.getSchema().equals(object_schema) && table.getName().equals(object_name)).findFirst();
            if (found_table.isPresent()) {
                Map<String, Object> additionalProps = new HashMap<>();
                additionalProps.put("shard_kind", resultSet.getString("shard_kind"));
                additionalProps.put("persistence", resultSet.getString("persistence"));
                found_table.get().setAdditionalProperties(additionalProps);
            }
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        return tables;
    }
}
