package com.adaptivescale.rosetta.common.models;

import java.util.HashMap;
import java.util.Map;

public class BaseModel {
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public void addProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Object getProperty(String name) {
        return this.additionalProperties.get(name);
    }

    public String getPropertyAsString(String name) {
        return (String) getProperty(name);
    }
}
