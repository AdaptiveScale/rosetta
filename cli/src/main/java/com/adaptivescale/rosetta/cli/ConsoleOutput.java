package com.adaptivescale.rosetta.cli;
import com.adaptivescale.rosetta.common.models.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConsoleOutput implements Output {

    @Override
    public void write(Database database) throws JsonProcessingException {
        String result = new ObjectMapper(new YAMLFactory()).writeValueAsString(database);
        System.out.println(result);
    }
}
