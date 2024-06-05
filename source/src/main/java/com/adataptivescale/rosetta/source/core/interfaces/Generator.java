package com.adataptivescale.rosetta.source.core.interfaces;

public interface Generator<V, E> {

    V generate(E inputSource) throws Exception;
    V validate(E inputSource) throws Exception;
}
