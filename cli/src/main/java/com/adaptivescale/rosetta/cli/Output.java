package com.adaptivescale.rosetta.cli;

public interface Output<T> {
    void write(T model) throws  Exception;
}
