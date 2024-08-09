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
import com.adaptivescale.rosetta.common.models.ColumnProperties;
import com.adaptivescale.rosetta.common.models.ForeignKey;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.COLUMN_EXTRACTOR
)
public class KineticaColumnsExtractor extends ColumnsExtractor {

    private static final List<String> KINETICA_PROPERTIES = Arrays.asList("DICT", "INIT_WITH_NOW", "INIT_WITH_UUID", "IPV4", "SHARD_KEY", "TEXT_SEARCH");

    public KineticaColumnsExtractor(Connection connection) {
        super(connection);
    }

    @Override
    protected void extract(ResultSet resultSet, Column column) throws SQLException {
        column.setName(resultSet.getString("COLUMN_NAME"));

        String columnType = String.valueOf(resultSet.getString("TYPE_NAME"));
        column.setTypeName(TranslationMatrix.getInstance().findBySourceTypeAndSourceColumnType("kinetica", columnType));

        column.setNullable(resultSet.getString("IS_NULLABLE").equals("YES"));
        column.setColumnDisplaySize(resultSet.getInt("COLUMN_SIZE"));
        column.setScale(resultSet.getInt("DECIMAL_DIGITS"));
        column.setPrecision(resultSet.getInt("COLUMN_SIZE"));

        String[] columnProperties = Optional.ofNullable(resultSet.getString("REMARKS"))
            .map(it -> it.replace("[", ""))
            .map(it -> it.replace("]", ""))
            .map(it -> it.split(","))
            .orElse(new String[0]);

        List<ColumnProperties> columnPropertiesList = new ArrayList<>();
        for (String columnProperty : columnProperties) {
            String trimmedProperty = StringUtils.trim(columnProperty);
            if (KINETICA_PROPERTIES.stream().anyMatch(trimmedProperty.toLowerCase()::equalsIgnoreCase)) {
                ColumnProperties cp = new ColumnProperties(trimmedProperty, null);
                columnPropertiesList.add(cp);
            }
        }

        column.setColumnProperties(columnPropertiesList);
    }

    @Override
    public void extract(java.sql.Connection connection, Collection<Table> tables) throws Exception {
        super.extract(connection, tables);

        for (Table table : tables) {
            Map<String, List<ForeignKey>> foreignKeys = extractForeignKeys(connection, table);
            table.getColumns().forEach(column -> {
                if (foreignKeys.containsKey(column.getName())) {
                    column.setForeignKeys(foreignKeys.get(column.getName()));
                }
            });
        }
    }

    private ResultSet fetchForeignKeys(java.sql.Connection connection, Table table) throws SQLException {
        String query = String.format(
                "select constraint_name as FK_NAME, schema_name as FKTABLE_SCHEM, table_name as FKTABLE_NAME, fk_column_name as FKCOLUMN_NAME, parent_schema_name as PKTABLE_SCHEM, parent_table_name as PKTABLE_NAME, pk_column_name as PKCOLUMN_NAME from ki_catalog.ki_fk_constraints " +
                        "where schema_name = '%s' and table_name = '%s' and fk_column_name is not null;", table.getSchema(), table.getName());
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        return resultSet;
    }

    private Map<String, List<ForeignKey>> extractForeignKeys(java.sql.Connection connection, Table table) throws SQLException {
        ResultSet exportedKeys = fetchForeignKeys(connection, table);
        Map<String, Set<ForeignKey>> result = new HashMap<>();

        while (exportedKeys.next()) {
            ForeignKey foreignKey = new ForeignKey();
            String pkTableSchema = exportedKeys.getString("PKTABLE_SCHEM");
            String fkTableSchema = exportedKeys.getString("FKTABLE_SCHEM");

            foreignKey.setName(exportedKeys.getString("FK_NAME"));
            foreignKey.setSchema(fkTableSchema);
            foreignKey.setTableName(exportedKeys.getString("FKTABLE_NAME"));
            foreignKey.setColumnName(exportedKeys.getString("FKCOLUMN_NAME"));

            foreignKey.setPrimaryTableSchema(pkTableSchema);
            foreignKey.setPrimaryTableName(exportedKeys.getString("PKTABLE_NAME"));
            foreignKey.setPrimaryColumnName(exportedKeys.getString("PKCOLUMN_NAME"));

            Set<ForeignKey> foreignKeys = result.computeIfAbsent(foreignKey.getColumnName(), k -> new HashSet<>());
            foreignKeys.add(foreignKey);
        }

        return result.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                ));
    }
}
