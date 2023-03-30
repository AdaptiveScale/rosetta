package com.adaptivescale.rosetta.common.models;

import com.adaptivescale.rosetta.common.models.enums.OperationLevelEnum;

import java.util.ArrayList;
import java.util.Collection;

public class Database {

    private Boolean safeMode = false;
    private Collection<Table> tables;
    private Collection<View> views = new ArrayList<>();
    private String databaseProductName;
    private String databaseType;
    private OperationLevelEnum operationLevel = OperationLevelEnum.database;

    public Collection<Table> getTables() {
        return tables;
    }

    public void setTables(Collection<Table> tables) {
        this.tables = tables;
    }

    public Collection<View> getViews() {
        return views;
    }

    public void setViews(Collection<View> views) {
        this.views = views;
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

    public Boolean getSafeMode() {
        return safeMode;
    }

    public void setSafeMode(Boolean safeMode) {
        this.safeMode = safeMode;
    }

    public OperationLevelEnum getOperationLevel() {
        return operationLevel;
    }

    public void setOperationLevel(OperationLevelEnum operationLevel) {
        this.operationLevel = operationLevel;
    }
}
