package com.adaptivescale.rosetta.cli.helpers;

import com.adaptivescale.rosetta.common.models.dbt.DbtModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbtEnhancedModelTransformer {

    /**
     * Generates enhanced SQL models from staging models based on dbt model definitions
     *
     * @param stagingSqlFiles List of staging SQL file paths
     * @param dbtModel        The DbtModel containing source and table definitions
     * @return Map of enhanced model names to enhanced SQL content
     */
    public static Map<String, String> enhancedSQLGenerator(List<Path> stagingSqlFiles, DbtModel dbtModel) {
        Map<String, String> tables = new HashMap<>();

        Map<String, Path> filesByTableName = stagingSqlFiles.stream()
                .collect(Collectors.toMap(
                        path -> path.getFileName().toString(),
                        path -> path
                ));

        dbtModel.getSources().forEach(dbtSource -> {
            dbtSource.getTables().forEach(dbtTable -> {
                String stagingKey = String.format("%s_%s.sql", dbtSource.getName(), dbtTable.getName());
                Path sqlFile = filesByTableName.get(stagingKey);

                if (sqlFile != null) {
                    try {
                        String stagingSqlContent = Files.readString(sqlFile);

                        StringBuilder enhancedSql = new StringBuilder();
                        enhancedSql.append("{{\n")
                                .append("    config(\n")
                                .append("        materialized='incremental',\n")
                                .append("        unique_key = ['UNIQUE_KEY_COLUMNS'],\n")
                                .append("    )\n")
                                .append("}}\n\n");

                        // Replace source() with ref()
                        String modifiedSql = stagingSqlContent.replaceAll(
                                "from \\{\\{\\s*source\\('([^']+)',\\s*'([^']+)'\\)\\s*\\}\\}",
                                "from {{ ref('$1_$2') }}"
                        );

                        if (!modifiedSql.contains("{% if is_incremental() %}")) {
                            int lastParenPos = modifiedSql.lastIndexOf(")");
                            if (lastParenPos > 0) {
                                String before = modifiedSql.substring(0, lastParenPos);
                                String after = modifiedSql.substring(lastParenPos);

                                String incrementalColumn = "INCREMENTAL_COLUMN";
                                enhancedSql.append(before)
                                        .append("\n\n{% if is_incremental() %}\n")
                                        .append(String.format("where %s > (select max(%s) from {{ this }})\n",
                                                incrementalColumn, incrementalColumn))
                                        .append("{% endif %}\n")
                                        .append(after);
                            } else {
                                enhancedSql.append(modifiedSql);
                            }
                        } else {
                            enhancedSql.append(modifiedSql);
                        }

                        tables.put("enh_" + dbtTable.getName(), enhancedSql.toString());

                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read staging SQL file: " + sqlFile, e);
                    }
                }
            });
        });

        return tables;
    }
}