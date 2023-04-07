package com.adaptivescale.rosetta.translation;

import com.adaptivescale.rosetta.translator.model.TranslateInfo;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TranslationTypoTest {

    @Test
    public void checkTranslationParsing() throws IOException {
        Path resourceDirectory = Paths.get("src", "main", "resources", "translations");
        Map<String, DatabindException> failedToParseMap = new HashMap<>();
        for (File translationFile : Objects.requireNonNull(resourceDirectory.toFile().listFiles())) {
            try {
                new ObjectMapper().readValue(translationFile, TranslateInfo.class);
            } catch (DatabindException e) {
                failedToParseMap.put(translationFile.getName(), e);
            }
        }

        if (!failedToParseMap.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            failedToParseMap.forEach((fileName, e) -> msg.append("Failed to parse translation: '")
                .append(fileName)
                .append("' cause:  ")
                .append(e.getMessage())
                .append("\n"));
            Assertions.fail(msg.toString());
        }
    }
}
