package com.adataptivescale.rosetta.source.dbt;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.dbt.DbtColumn;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.adaptivescale.rosetta.common.models.dbt.DbtSource;
import com.adaptivescale.rosetta.common.models.dbt.DbtTable;
import com.adaptivescale.rosetta.common.models.input.Connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DbtModelGenerator {
  public static DbtModel dbtModelGenerator(Connection connection, List<Database> databases) {
    DbtModel dbtModel = new DbtModel();
    dbtModel.setVersion(2);
    Collection<DbtSource> dbtSources = new ArrayList<>();
    databases.forEach(database -> {
      prepareDbtSource(connection, dbtSources, database);
    });
    dbtModel.setSources(dbtSources);
    return dbtModel;
  }

  private static void prepareDbtSource(Connection connection, Collection<DbtSource> dbtSources, Database database) {
    DbtSource dbtSource = new DbtSource();
    dbtSource.setName(connection.getSchemaName());
    dbtSource.setDescription(database.getDatabaseType());

    Collection<DbtTable> dbtTables = new ArrayList<>();

    database.getTables().forEach(table -> {
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
  }

  public static Map<String, String> dbtSQLGenerator(DbtModel dbtModel) {
    Map<String, String> tables = new HashMap<>();

    dbtModel.getSources().forEach(dbtSource -> {
      dbtSource.getTables().forEach(dbtTable -> {
        StringBuilder table = new StringBuilder();
        table.append("with ").append(dbtTable.getName()).append(" as (");
        table.append("\n\t");
        table.append("select\n\t\t");
        table.append(dbtTable.getColumns().stream().map(DbtColumn::getName).collect(Collectors.joining(",\n\t\t")));
        table.append("\n\t");
        table.append(String.format("from {{ source('%s', '%s') }}", dbtSource.getName(), dbtTable.getName()));
        table.append("\n)\n\n");
        table.append("select * from ").append(dbtTable.getName());
        tables.put(String.format("%s_%s", dbtSource.getName(), dbtTable.getName()), String.valueOf(table));
      });
    });

    return tables;
  }
}