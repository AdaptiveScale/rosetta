package com.adaptivescale.rosetta.cli.model;

public class TableAppend {
    private String schema;
    private String name;
    private String tableName;
    private String preScript;
    private String postScript;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPostScript() {
        return postScript;
    }

    public void setPostScript(String postScript) {
        this.postScript = postScript;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
