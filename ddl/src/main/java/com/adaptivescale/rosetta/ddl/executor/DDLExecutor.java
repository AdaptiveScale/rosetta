package com.adaptivescale.rosetta.ddl.executor;


import java.sql.SQLException;

public interface DDLExecutor {
     void execute(String query) throws SQLException;
}
