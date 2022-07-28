package com.adaptivescale.rosetta.test.assertion.output;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;

public interface Output {

    void startTestForDatabase(String name, int size);

    long printStartTest(AssertTest assertion, Column column);

    void printEndTest(AssertTest assertion, Column column, long startTime, boolean pass, String result);

    void endTestForDatabase();

}
