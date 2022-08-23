package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Utils {
    private Utils(){
        //no op
    }

    static Database getDatabase(Path resourceDirectory, String resourceName) throws IOException {
        File file = resourceDirectory.resolve(resourceName).toFile();
        return new ObjectMapper(new YAMLFactory()).readValue(file, Database.class);
    }
}
