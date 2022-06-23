package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.ddl.targets.bigquery.BigQueryDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.mysql.MySqlDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.snowflake.SnowflakeDDLGenerator;

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
}
