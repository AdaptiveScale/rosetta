package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;

public interface AssertionSqlGenerator {
     String generateSql(Connection connection, Table table, Column column, AssertTest assertion);
}
