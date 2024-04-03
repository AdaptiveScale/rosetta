package com.adaptivescale.rosetta.cli.model;

import com.adaptivescale.rosetta.common.models.input.Connection;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public class Config {
    private List<Connection> connections;

    @JsonProperty("OpenAI_API_KEY")
    private String OpenAI_API_KEY;

    @JsonProperty("OpenAI_Model")
    private String OpenAI_Model;

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connection) {
        this.connections = connection;
    }

    public Optional<Connection> getConnection(String name) {
        return connections.stream().filter(target -> target.getName().equals(name)).findFirst();
    }

    public String getOpenAI_API_KEY() {
        return OpenAI_API_KEY;
    }
    public String getOpenAI_Model() { return OpenAI_Model; }
}
