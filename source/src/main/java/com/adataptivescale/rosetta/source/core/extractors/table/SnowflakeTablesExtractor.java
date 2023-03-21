package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

@RosettaModule(
        name = "snowflake",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)
public class SnowflakeTablesExtractor extends DefaultTablesExtractor{
}
