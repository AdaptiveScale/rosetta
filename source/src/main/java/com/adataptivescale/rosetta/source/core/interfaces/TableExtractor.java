package com.adataptivescale.rosetta.source.core.interfaces;


public interface TableExtractor<V,E,U> {

     V extract(E param1, U param2) throws Exception;
}
