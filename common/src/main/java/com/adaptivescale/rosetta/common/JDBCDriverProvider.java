package com.adaptivescale.rosetta.common;

import com.adaptivescale.rosetta.common.models.input.Connection;
import java.sql.Driver;
import java.sql.SQLException;

public interface JDBCDriverProvider {
    Driver getDriver(Connection connection) throws SQLException;
}
