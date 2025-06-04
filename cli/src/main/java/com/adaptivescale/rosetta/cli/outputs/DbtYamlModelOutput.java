package com.adaptivescale.rosetta.cli.outputs;

import com.adaptivescale.rosetta.cli.Output;
import com.adaptivescale.rosetta.common.models.dbt.DbtModel;
import com.adaptivescale.rosetta.common.models.dbt.DbtSource;
import com.adaptivescale.rosetta.common.models.dbt.DbtTable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class DbtYamlModelOutput implements Output<DbtModel> {
  private final Path directory;

  public DbtYamlModelOutput(Path directory) {
    this.directory = directory;
  }

  @Override
  public void write(DbtModel model) {
    ObjectMapper mapper = new ObjectMapper(
            new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
    );

    for (DbtSource source : model.getSources()) {
      String sourceName = source.getName();

      for (DbtTable table : source.getTables()) {
        String tableName = table.getName();
        String fileName = String.format("%s_%s.yaml", sourceName, tableName);
        Path tablePath = directory.resolve(fileName);

        DbtModel tableModel = new DbtModel();
        tableModel.setVersion(model.getVersion());

        DbtSource tableSource = new DbtSource();
        tableSource.setName(sourceName);
        tableSource.setDescription(source.getDescription());
        tableSource.setTables(Collections.singletonList(table));

        tableModel.setSources(Collections.singletonList(tableSource));

        try {
          mapper.writeValue(tablePath.toFile(), tableModel);
        } catch (IOException e) {
          throw new RuntimeException("Failed to write YAML for table: " + tableName, e);
        }
      }
    }
  }

  public void writeEnhanced(DbtModel model) {
    ObjectMapper mapper = new ObjectMapper(
            new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
    );

    for (DbtSource source : model.getSources()) {
      String sourceName = source.getName();

      for (DbtTable table : source.getTables()) {
        String tableName = table.getName();
        String fileName = String.format("%s_%s.yaml", "enh", tableName);
        Path tablePath = directory.resolve(fileName);

        DbtModel tableModel = new DbtModel();
        tableModel.setVersion(model.getVersion());

        DbtSource tableSource = new DbtSource();
        tableSource.setName(sourceName);
        tableSource.setDescription(source.getDescription());
        tableSource.setTables(Collections.singletonList(table));

        tableModel.setSources(Collections.singletonList(tableSource));

        try {
          mapper.writeValue(tablePath.toFile(), tableModel);
        } catch (IOException e) {
          throw new RuntimeException("Failed to write YAML for table: " + tableName, e);
        }
      }
    }
  }
}