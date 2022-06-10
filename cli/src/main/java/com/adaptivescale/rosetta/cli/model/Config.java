package com.adaptivescale.rosetta.cli.model;

import com.adaptivescale.rosetta.common.models.input.Connection;

import java.util.List;
import java.util.Optional;

public class Config {
    private List<Connection> connections;

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connection) {
        this.connections = connection;
    }

    public Optional<Connection> getConnection(String name) {
        return connections.stream().filter(target -> target.getName().equals(name)).findFirst();
    }
}
