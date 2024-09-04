package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)
public class KineticaTablesExtractor extends DefaultTablesExtractor {
    @Override
    public Collection<Table> extract(Connection target, java.sql.Connection connection) throws SQLException {
        Collection<Table> tables = super.extract(target, connection);
        attachTablePartitions(tables, connection);
        attachTableType(tables, connection);
        return tables;
    }

    private void attachTableType(Collection<Table> tables, java.sql.Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT object_name, schema_name, shard_kind, persistence FROM ki_catalog.ki_objects;");

        while (resultSet.next()) {
            String object_schema = resultSet.getString("schema_name");
            String object_name = resultSet.getString("object_name");
            Optional<Table> found_table = tables.stream().filter(table -> table.getSchema().equals(object_schema) && table.getName().equals(object_name)).findFirst();
            if (found_table.isPresent()) {
                found_table.get().addProperty("shard_kind", resultSet.getString("shard_kind"));
                found_table.get().addProperty("persistence", resultSet.getString("persistence"));
            }
        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
    }

    private void attachTablePartitions(Collection<Table> tables, java.sql.Connection connection) {
        for (Table table : tables) {
            String queryTemplate = "SHOW %s.%s;";
            try (Statement statement = connection.createStatement()) {
                String query = String.format(queryTemplate, table.getSchema(), table.getName());
                ResultSet resultSet = statement.executeQuery(query);
                List<Map<String, Object>> records = QueryHelper.mapRecords(resultSet);

                if (!records.isEmpty()) {
                    table.addProperty("partitions", extractPartitions(records.get(0).get("ddl").toString()));
                }

            } catch (SQLException e) {
                log.warn("Failed attaching partitions for table {}.{} due to: {}", table.getSchema(), table.getName(), e.getMessage());
            }
        }
    }

    private String extractPartitions(String ddl) {
        // Regular expression pattern to match the PARTITION BY and PARTITIONS block
        String patternString = "(PARTITION BY\\s+\\w+\\s*\\(.*?\\)\\s*PARTITIONS\\s*\\(.*?\\))(?:\\s*TIER STRATEGY|;)";
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(ddl);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
}
