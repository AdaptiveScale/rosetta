package com.adaptivescale.rosetta.translator;

public interface Translator<V, R> {

    R translate(V input) throws Exception;
}
