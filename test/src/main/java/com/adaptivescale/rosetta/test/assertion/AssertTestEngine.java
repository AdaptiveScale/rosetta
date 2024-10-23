package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.input.Connection;

public interface AssertTestEngine {
    void run(Connection connection, Database database);
    void run(Connection sourceConnection, Connection targetConnection, Database sourceDatabase);
}
