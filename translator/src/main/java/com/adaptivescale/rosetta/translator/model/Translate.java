package com.adaptivescale.rosetta.translator.model;

import java.util.Collection;

public class Translate {

    private String targetTypeName;
    private int length;
    private Collection<CompatibleType> compatibleTypes;

    public Collection<CompatibleType> getCompatibleTypes() {
        return compatibleTypes;
    }

    public void setTargetTypeName(String targetTypeName) {
        this.targetTypeName = targetTypeName;
    }

    public void setCompatibleTypes(Collection<CompatibleType> compatibleTypes) {
        this.compatibleTypes = compatibleTypes;
    }

    public String getTargetTypeName() {
        return targetTypeName;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
