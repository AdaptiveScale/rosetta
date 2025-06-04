package com.adataptivescale.rosetta.source.core.extractors.column;


import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

@RosettaModule(
        name = "databricks",
        type = RosettaModuleTypes.COLUMN_EXTRACTOR
)
public class DatabricksColumnsExtractor extends ColumnsExtractor{
    public DatabricksColumnsExtractor(Connection connection) {
        super(connection);
    }
}
