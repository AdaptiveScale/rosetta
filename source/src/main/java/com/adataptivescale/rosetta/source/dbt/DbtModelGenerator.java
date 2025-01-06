package com.adataptivescale.rosetta.source.dbt;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.dbt.DbtColumn;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.adaptivescale.rosetta.common.models.dbt.DbtSource;
import com.adaptivescale.rosetta.common.models.dbt.DbtTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DbtModelGenerator {
  public static DbtModel dbtModelGenerator(List<Database> databases) {
    DbtModel dbtModel = new DbtModel();
    dbtModel.setVersion(2);
    Collection<DbtSource> dbtSources = new ArrayList<>();
    databases.forEach(database -> {
      prepareDbtSource(dbtSources, database);
    });
    dbtModel.setSources(dbtSources);
    return dbtModel;
  }

  private static void prepareDbtSource(Collection<DbtSource> dbtSources, Database database) {
    // group tables to their specific schema
    Map<String, List<Table>> tablesBySchema = database.getTables().stream().collect(Collectors.groupingBy(Table::getSchema));

    // map every table to dbtTable and assign it to its specific schema
    tablesBySchema.forEach((schemaName, tables) -> {
      DbtSource dbtSource = new DbtSource();
      dbtSource.setName(schemaName);
      dbtSource.setDescription(database.getDatabaseType());
      Collection<DbtTable> dbtTables = new ArrayList<>();

      tables.forEach(table -> {
        DbtTable dbtTable = new DbtTable();
        dbtTable.setName(table.getName());
        Collection<DbtColumn> dbtColumns = new ArrayList<>();

        table.getColumns().forEach(column -> {
          Collection<String> tests = new ArrayList<>();
          DbtColumn dbtColumn = new DbtColumn();
          dbtColumn.setName(column.getName());
          dbtColumn.setDescription(column.getDescription());
          if (!column.isNullable()) {
            tests.add("not_null");
          }
          if (column.isPrimaryKey()) {
            tests.add("unique");
          }
          dbtColumn.setTests(tests);
          dbtColumns.add(dbtColumn);
        });
        dbtTable.setColumns(dbtColumns);
        dbtTables.add(dbtTable);
      });
      dbtSource.setTables(dbtTables);
      dbtSources.add(dbtSource);
    });
  }

  public static Map<String, String> dbtSQLGenerator(DbtModel dbtModel, Boolean isIncremental) {
    Map<String, String> tables = new HashMap<>();

    dbtModel.getSources().forEach(dbtSource -> {
      dbtSource.getTables().forEach(dbtTable -> {
        StringBuilder table = new StringBuilder();

        if (isIncremental) {
            try {
                generateIncrementalHeader(dbtTable, table);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        table.append("with ").append(dbtTable.getName()).append(" as (");
        table.append("\n\t");
        table.append("select\n\t\t");
        table.append(dbtTable.getColumns().stream().map(DbtColumn::getName).collect(Collectors.joining(",\n\t\t")));
        table.append("\n\t");

        if (isIncremental) {
          table.append(String.format("from {{ ref('%s_%s') }}", dbtSource.getName(), dbtTable.getName()));
        }
        else {
          table.append(String.format("from {{ source('%s', '%s') }}", dbtSource.getName(), dbtTable.getName()));
        }

        if (isIncremental) {
            try {
                generateIncrementalCondition(dbtTable, table);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        table.append("\n)\n\n");
        table.append("select * from ").append(dbtTable.getName());
        tables.put(String.format("%s_%s", dbtSource.getName(), dbtTable.getName()), String.valueOf(table));
      });
    });

    return tables;
  }

  private static void generateIncrementalHeader(DbtTable dbtTable, StringBuilder table) throws IOException {
    System.out.println("\n=======================================");
    System.out.println("Table Name: " + dbtTable.getName());
    System.out.println("=======================================\n");
    System.out.println("List of Columns:\n");

    AtomicInteger columnIndex = new AtomicInteger(1);
    String columnOutput = dbtTable.getColumns().stream()
            .map(column -> String.format("\t%d. %s", columnIndex.getAndIncrement(), column.getName()))
            .collect(Collectors.joining("\n"));

    System.out.println(columnOutput);
    System.out.println("\n---------------------------------------");
    System.out.print("Please type the list of columns that represent the unique_key (split by comma): ");
    String uniqueKeysInput = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
    table.append("{{\n")
            .append("    config(\n")
            .append("        materialized='incremental',\n")
            .append(String.format("        unique_key = [%s],\n",
                    Arrays.stream(uniqueKeysInput.split(","))
                            .map(String::trim)
                            .map(key -> "'" + key + "'")
                            .collect(Collectors.joining(", "))))
            //                .append(String.format("        alias = '%s'\n", dbtTable.getName()))
            .append("    )\n")
            .append("}}\n\n");
  }

  private static void generateIncrementalCondition(DbtTable dbtTable, StringBuilder table) throws IOException {
    System.out.println("\n=======================================");
    System.out.println("Table Name: " + dbtTable.getName());
    System.out.println("=======================================\n");
    System.out.println("List of Columns:\n");
    AtomicInteger columnIndex = new AtomicInteger(1);
    String columnOutput = dbtTable.getColumns().stream()
            .map(column -> String.format("\t%d. %s", columnIndex.getAndIncrement(), column.getName()))
            .collect(Collectors.joining("\n"));

    System.out.println(columnOutput);
    System.out.println("\n---------------------------------------");
    System.out.println("Please type the column used for the incremental load:");
    String incrementalColumn = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
    table.append("\n\n\t{% if is_incremental() -%}\n\t");
    table.append(String.format("where %s > (select max(%s) from {{ this }})",
            incrementalColumn, incrementalColumn));
    table.append("\n\t{%- endif %}\n");
  }
}