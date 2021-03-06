package com.adaptivescale.rosetta.translator;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.translator.model.TranslateInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class TranslatorFactory {

   public static Translator<Database, Database> translator(String sourceDBName, String targetDBName) throws IOException {
        String resourceName = String.format("translations/%s_%s.json", sourceDBName, targetDBName);
        InputStream resourceAsStream = TranslatorFactory.class.getClassLoader().getResourceAsStream(resourceName);
        TranslateInfo translateInfo = new ObjectMapper().readValue(resourceAsStream, TranslateInfo.class);
        return new DefaultTranslator(translateInfo, targetDBName);
    }
}
