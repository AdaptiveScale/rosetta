package com.adaptivescale.rosetta.common.models;

import java.util.Map;

public class AbstractModel {
    private Map<String, Object> additionalProperties;

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Object getProperty(String name) {
        if (this.additionalProperties != null)
            return this.additionalProperties.get(name);
        return null;
    }

    public String getPropertyAsString(String name) {
        return (String) getProperty(name);
    }
}
