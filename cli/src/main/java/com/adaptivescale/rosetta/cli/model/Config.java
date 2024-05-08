package com.adaptivescale.rosetta.cli.model;

import com.adaptivescale.rosetta.common.models.input.Connection;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public class Config {
    private List<Connection> connections;

    @JsonProperty("openai_api_key")
    private String openAIApiKey;

    @JsonProperty("openai_model")
    private String openAIModel;

    @JsonProperty("git_auto_commit")
    private boolean gitAutoCommit = false;
    @JsonProperty("git_remote_name")
    private String gitRemoteName = "origin";

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connection) {
        this.connections = connection;
    }

    public Optional<Connection> getConnection(String name) {
        return connections.stream().filter(target -> target.getName().equals(name)).findFirst();
    }

    public String getOpenAIApiKey() {
        return openAIApiKey;
    }
    public String getOpenAIModel() { return openAIModel; }

    public String getGitRemoteName() {
        return gitRemoteName;
    }
    public boolean isAutoCommit() {
        return gitAutoCommit;
    }

}
