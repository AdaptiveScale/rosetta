package com.adaptivescale.rosetta.translator;

import com.adaptivescale.rosetta.common.models.Database;

import java.io.IOException;

public class TranslatorFactory {

   public static Translator<Database, Database> translator(String sourceDBName, String targetDBName) throws IOException {
       return new DefaultTranslator(sourceDBName, targetDBName);
    }
}
