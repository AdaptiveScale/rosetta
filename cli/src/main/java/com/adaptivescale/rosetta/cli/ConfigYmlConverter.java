package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.cli.model.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.text.StringSubstitutor;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class ConfigYmlConverter implements CommandLine.ITypeConverter<Config> {
    @Override
    public Config convert(String value) throws Exception {
        File file = new File(value);
        if(!file.exists()){
            return null;
        }
        return new ObjectMapper(new YAMLFactory()).readValue(processEnvParameters(file), Config.class);
    }

    private String processEnvParameters(File file) throws IOException {
        String content = Files.readString(file.toPath());
        StringSubstitutor stringSubstitutor = new StringSubstitutor(System.getenv(), "${", "}");
        return stringSubstitutor.replace(content);
    }
}
