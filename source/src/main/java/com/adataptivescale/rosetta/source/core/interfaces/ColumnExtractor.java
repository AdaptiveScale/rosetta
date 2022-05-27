package com.adataptivescale.rosetta.source.core.interfaces;

public interface ColumnExtractor<V,U> {
    void extract(V param1, U param2) throws Exception;
}
