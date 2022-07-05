package com.adaptivescale.rosetta.common.models.input;

import java.util.ArrayList;
import java.util.Collection;

public class Connection {

    private String name;
    private String databaseName;
    private String schemaName;
    private String dbType;
    private String url;
    private String userName;
    private String password;
    private Collection<String> tables = new ArrayList<>();

    public Connection() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> getTables() {
        return tables;
    }

    public void setTables(Collection<String> tables) {
        this.tables = tables;
    }
}
