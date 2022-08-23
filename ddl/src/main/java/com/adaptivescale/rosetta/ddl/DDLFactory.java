package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.change.ChangeHandler;
import com.adaptivescale.rosetta.ddl.change.ChangeHandlerImplementation;
import com.adaptivescale.rosetta.ddl.change.SnowflakeChangeComparator;
import com.adaptivescale.rosetta.ddl.targets.bigquery.BigQueryDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.mysql.MySqlDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.snowflake.SnowflakeDDLGenerator;

import java.util.Comparator;

public class DDLFactory {

    public static DDL ddlForDatabaseType(String databaseType) {
        switch (databaseType) {
            case "mysql":
                return new MySqlDDLGenerator();
            case "snowflake":
                return new SnowflakeDDLGenerator();
            case "bigquery":
                return new BigQueryDDLGenerator();
            default:
                throw new RuntimeException("DDL not supported for database type: " + databaseType);
        }
    }

    public static ChangeHandler changeHandler(String databaseType) {
        DDL ddl = ddlForDatabaseType(databaseType);
        Comparator<Change<?>> changeComparator = comparatorForDatabase(databaseType);
        return new ChangeHandlerImplementation(ddl, changeComparator);
    }

    private static Comparator<Change<?>> comparatorForDatabase(String databaseType) {
        if ("snowflake".equals(databaseType)) {
            return new SnowflakeChangeComparator();
        }
        return null;
    }
}
