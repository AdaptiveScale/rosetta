package com.adaptivescale.rosetta.translator.model;

import java.util.Collection;

public class ConvertType {

    private String targetTypeName;
    private int length;
    private double precision;
    private double scale;

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

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
