package com.adaptivescale.rosetta.translator.model;

import java.util.Collection;

public class TranslateInfo {
    private String version;
    private Collection<Translate> translates;

    public String getVersion() {
        return version;
    }

    public Collection<Translate> getTranslates() {
        return translates;
    }

}
