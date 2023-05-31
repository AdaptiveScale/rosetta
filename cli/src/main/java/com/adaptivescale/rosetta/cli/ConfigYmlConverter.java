package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.text.StringSubstitutor;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class ConfigYmlConverter implements CommandLine.ITypeConverter<Config> {
    @Override
    public Config convert(String value) throws Exception {
        File file = new File(value);
        if(!file.exists()){
            return null;
        }

        final String processedFileWithEnvParameters = processEnvParameters(file);
        return processConfigParameters(processedFileWithEnvParameters);
    }

    private String processEnvParameters(File file) throws IOException {
        String content = Files.readString(file.toPath());
        StringSubstitutor stringSubstitutor = new StringSubstitutor(System.getenv(), "${", "}");
        return stringSubstitutor.replace(content);
    }

    private Config processConfigParameters(String configContent) throws IOException {
        Config config = new ObjectMapper(new YAMLFactory()).readValue(configContent, Config.class);
        for (Connection connection : config.getConnections()) {
            Map<String, String> configParameters = connection.toMap();
            StringSubstitutor stringSubstitutor = new StringSubstitutor(configParameters, "${", "}");
            String processedUrl = stringSubstitutor.replace(connection.getUrl());
            connection.setUrl(processedUrl);
        }

        return config;
    }
}
