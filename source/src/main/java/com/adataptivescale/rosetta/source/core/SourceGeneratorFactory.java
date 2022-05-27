package com.adataptivescale.rosetta.source.core;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Target;
import com.adataptivescale.rosetta.source.core.interfaces.Generator;

public class SourceGeneratorFactory {

    public static Generator<Database, Target> sourceGenerator(Target target) {
        TablesExtractor tablesExtractor = new TablesExtractor();
        ColumnsExtractor columnsExtractor = new ColumnsExtractor(target);
        return new DefaultGenerator(tablesExtractor, columnsExtractor);
    }
}
