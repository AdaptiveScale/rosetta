package com.adaptivescale.rosetta.cli.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbtEnhancedModelTransformer {

    /**
     * Generates enhanced SQL models from staging SQL files
     *
     * @param stagingSqlFiles List of staging SQL file paths
     * @param rawPrefix Prefix for enhanced model names
     * @return Map of enhanced model names to enhanced SQL content
     */
    public static Map<String, String> enhancedSQLGenerator(List<Path> stagingSqlFiles, String rawPrefix) {
        Map<String, String> sqlContent = new HashMap<>();

        // Read all files into memory
        for (Path sqlFile : stagingSqlFiles) {
            String fileName = sqlFile.getFileName().toString();
            if (!fileName.endsWith(".sql")) continue;

            // remove .sql from file name
            String baseName = fileName.substring(0, fileName.length() - 4);

            try {
                String content = Files.readString(sqlFile);
                sqlContent.put(baseName, content);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read staging SQL file: " + sqlFile, e);
            }
        }

        // Use the core transformation logic
        return transformToEnhancedSQL(sqlContent, rawPrefix);
    }

    /**
     * Generates enhanced SQL models from in-memory SQL content
     *
     * @param sqlModels Map of model names to SQL content
     * @param rawPrefix Prefix for enhanced model names
     * @return Map of enhanced model names to enhanced SQL content
     */
    public static Map<String, String> enhancedSQLGenerator(Map<String, String> sqlModels, String rawPrefix) {
        return transformToEnhancedSQL(sqlModels, rawPrefix);
    }

    /**
     * Core transformation logic that converts staging SQL to enhanced SQL
     * This is where all the actual work happens
     *
     * @param sqlContent Map of model names to SQL content
     * @param rawPrefix Prefix for enhanced model names
     * @return Map of enhanced model names to enhanced SQL content
     */
    private static Map<String, String> transformToEnhancedSQL(Map<String, String> sqlContent, String rawPrefix) {
        String prefix = sanitizePrefix(rawPrefix);
        Map<String, String> enhancedSql = new HashMap<>();

        for (Map.Entry<String, String> entry : sqlContent.entrySet()) {
            String modelName = entry.getKey();
            String stagingSqlContent = entry.getValue();

            StringBuilder enhancedSqlBuilder = new StringBuilder();

            // Add DBT config block
            enhancedSqlBuilder.append("{{\n")
                    .append("    config(\n")
                    .append("        materialized='incremental',\n")
                    .append("        unique_key = ['UNIQUE_KEY_COLUMNS'],\n")
                    .append("    )\n")
                    .append("}}\n\n");

            String modifiedSql = stagingSqlContent.replaceAll(
                    "from \\{\\{\\s*source\\('([^']+)',\\s*'([^']+)'\\)\\s*\\}\\}",
                    "from {{ ref('" + modelName + "') }}"
            );

            // Add incremental logic if not already present
            if (!modifiedSql.contains("{% if is_incremental() %}")) {
                modifiedSql = addIncrementalLogic(modifiedSql);
            }

            enhancedSqlBuilder.append(modifiedSql);
            enhancedSql.put(prefix + "_" + modelName, enhancedSqlBuilder.toString());
        }

        return enhancedSql;
    }

    /**
     * Adds incremental logic to SQL if not already present
     */
    private static String addIncrementalLogic(String sql) {
        int lastParenPos = sql.lastIndexOf(")");
        if (lastParenPos > 0) {
            String before = sql.substring(0, lastParenPos);
            String after = sql.substring(lastParenPos);
            String incrementalColumn = "INCREMENTAL_COLUMN";

            return before +
                    "\n\n{% if is_incremental() %}\n" +
                    String.format("where %s > (select max(%s) from {{ this }})\n", incrementalColumn, incrementalColumn) +
                    "{% endif %}\n" +
                    after;
        } else {
            return sql;
        }
    }

    /**
     * Sanitizes the prefix to be a valid identifier
     */
    private static String sanitizePrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isBlank()) return "enh";

        // replace any non-word character with underscore
        String sanitized = rawPrefix.replaceAll("[^A-Za-z0-9_]", "_");

        // remove trailing underscores
        while (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }

        return sanitized.isEmpty() ? "enh" : sanitized;
    }
}