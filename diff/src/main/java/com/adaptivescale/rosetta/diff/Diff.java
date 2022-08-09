package com.adaptivescale.rosetta.diff;

public interface Diff<R,V,B> {

    R find(V localValue, B targetValue);
}
