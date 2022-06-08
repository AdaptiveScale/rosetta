package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.common.models.Database;

import java.io.IOException;

public interface Output<T> {

    void write(T model) throws  Exception;
}
