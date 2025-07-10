package com.adaptivescale.rosetta.cli.helpers;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.common.models.input.Connection;

public class CliHelper {

    public static void requireConfig(Config config) {
        if (config == null) {
            throw new RuntimeException("Missing required config. Use -c to provide a config file.");
        }
    }

    public static Connection getConnection(Config config, String name) {
        return config.getConnection(name)
                .orElseThrow(() -> new RuntimeException("Connection not found: " + name));
    }
}
