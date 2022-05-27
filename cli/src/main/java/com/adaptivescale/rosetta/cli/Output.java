package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.common.models.Database;

import java.io.IOException;

public interface Output {

    void write(Database database) throws  Exception;
}
