package com.adaptivescale.rosetta.cli;

import com.adaptivescale.rosetta.common.TranslationMatrix;
import picocli.CommandLine;

public class Main {
    public static void main(String... args) {
        int exitCode = new CommandLine(new Cli()).execute(args);
        System.exit(exitCode);
    }
}
