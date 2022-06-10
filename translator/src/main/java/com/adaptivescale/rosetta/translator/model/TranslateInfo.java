package com.adaptivescale.rosetta.translator.model;

import java.util.Collection;

public class TranslateInfo {
    private String version;
    private Collection<ConvertType> converters;

    public String getVersion() {
        return version;
    }

    public Collection<ConvertType> getConverters() {
        return converters;
    }

}
