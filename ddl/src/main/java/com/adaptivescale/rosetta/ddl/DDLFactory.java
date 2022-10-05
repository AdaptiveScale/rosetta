package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.BigQueryChangesComparator;
import com.adaptivescale.rosetta.ddl.change.comparator.MysqlForeignKeyChangeComparator;
import com.adaptivescale.rosetta.ddl.change.comparator.SnowflakeChangesComparator;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.*;
import com.adaptivescale.rosetta.ddl.targets.bigquery.BigQueryDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.mysql.MySqlDDLGenerator;
import com.adaptivescale.rosetta.ddl.targets.postgres.PostgresDDLGenerator;
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
            case "postgres":
                return new PostgresDDLGenerator();
            default:
                throw new RuntimeException("DDL not supported for database type: " + databaseType);
        }
    }

    public static DDLExecutor executor(Connection connection, JDBCDriverProvider driverProvider) {
        String dbType = connection.getDbType();
        switch (dbType) {
            case "bigquery":
                return new BigQueryDDLExecutor(connection, driverProvider);
            case "snowflake":
                return new SnowflakeDDLExecutor(connection, driverProvider);
            case "mysql":
                return new MySqlDDLExecutor(connection, driverProvider);
            case "postgres":
                return new PostgresDDLExecutor(connection, driverProvider);
            default:
                throw new RuntimeException("DDL not supported for database type: " + dbType);
        }
    }

    public static ChangeHandler changeHandler(String databaseType) {
        DDL ddl = ddlForDatabaseType(databaseType);
        Comparator<Change<?>> changeComparator = changesSortComparatorForDatabase(databaseType);
        return new ChangeHandlerImplementation(ddl, changeComparator);
    }

    private static Comparator<Change<?>> changesSortComparatorForDatabase(String databaseType) {
        if ("bigquery".equals(databaseType)) {
            return new BigQueryChangesComparator();
        }
        if ("snowflake".equals(databaseType)) {
            return new SnowflakeChangesComparator();
        }
        if ("mysql".equals(databaseType)) {
            return new MysqlForeignKeyChangeComparator();
        }
        return null;
    }

    public static ChangeFinder changeFinderForDatabaseType(String databaseType) {
        if ("mysql".equals(databaseType)) {
            return new MySQLChangeFinder();
        }
        return new DefaultChangeFinder();
    }


}
