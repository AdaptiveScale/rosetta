package com.adaptivescale.rosetta.cli.outputs;
import com.adaptivescale.rosetta.cli.Output;

import java.nio.file.Files;
import java.nio.file.Path;

public class StringOutput implements Output<String> {
    private final Path filePath;

    public Path getFilePath() {
        return filePath;
    }

    public StringOutput(String fileName, Path directory) {
        this.filePath = directory.resolve(fileName);
    }

    @Override
    public void write(String ddl) throws Exception {
        byte[] strToBytes = ddl.getBytes();
        Files.write(filePath, strToBytes);
    }
}
