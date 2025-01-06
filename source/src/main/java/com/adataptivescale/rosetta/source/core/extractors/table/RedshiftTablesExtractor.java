package com.adataptivescale.rosetta.source.core.extractors.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

@RosettaModule(
        name = "redshift",
        type = RosettaModuleTypes.TABLE_EXTRACTOR
)

public class RedshiftTablesExtractor extends DefaultTablesExtractor {

}
