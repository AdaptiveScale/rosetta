package com.adaptivescale.rosetta.cli.helpers;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbtEnhancedModelTransformer {

    /**
     * Generates enhanced SQL models from staging models
     *
     * @param stagingSqlFiles List of staging SQL file paths
     * @return Map of enhanced model names to enhanced SQL content
     */
    public static Map<String, String> enhancedSQLGenerator(List<Path> stagingSqlFiles, String rawPrefix) {
        String prefix = sanitizePrefix(rawPrefix);
        Map<String, String> tables = new HashMap<>();

        for (Path sqlFile : stagingSqlFiles) {
            String fileName = sqlFile.getFileName().toString();
            if (!fileName.endsWith(".sql")) continue;

            // remove .sql from file name
            String baseName = fileName.substring(0, fileName.length() - 4);

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

                tables.put(prefix + "_" + baseName, enhancedSql.toString());

            } catch (IOException e) {
                throw new RuntimeException("Failed to read staging SQL file: " + sqlFile, e);
            }
        }

        return tables;
    }

    private static String sanitizePrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isBlank()) return "enh";

        // replace any non-word character with underscore
        String sanitized = rawPrefix.replaceAll("[^A-Za-z0-9_]", "_");

        // remove trailing _
        while (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        return sanitized.isEmpty() ? "enh" : sanitized;
    }
}