package com.adaptivescale.rosetta.common.models;

import java.util.Collection;

public class Database {

    private Boolean safetyOperationEnabled = false;
    private Collection<Table> tables;
    private String databaseProductName;
    private String databaseType;

    public Collection<Table> getTables() {
        return tables;
    }

    public void setTables(Collection<Table> tables) {
        this.tables = tables;
    }

    public void setName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public Boolean getSafetyOperationEnabled() {
        return safetyOperationEnabled;
    }

    public void setSafetyOperationEnabled(Boolean safetyOperationEnabled) {
        this.safetyOperationEnabled = safetyOperationEnabled;
    }
}
