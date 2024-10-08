package com.adaptivescale.rosetta.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AbstractModel {
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
        if (additionalProperties.containsKey(name))
            return this.additionalProperties.get(name);
        return null;
    }

    public String getPropertyAsString(String name) {
        return (String) getProperty(name);
    }
}
