package com.adaptivescale.rosetta.test;

public interface Tester<R,V,B> {

    R test(V localValue, B targetValue);
}
