package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.io.File;


public class ConfigYmlConverter implements CommandLine.ITypeConverter<Config> {
    @Override
    public Config convert(String value) throws Exception {
        File file = new File(value);
        return new ObjectMapper(new YAMLFactory()).readValue(file, Config.class);
    }
}
