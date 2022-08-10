package com.adaptivescale.rosetta.test.assertion.output;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleOutput implements Output {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("hh:mm:ss");
    private int runningTestIndex;
    private int totalTestCount;

    @Override
    public void startTestForDatabase(String name, int testSize) {
        print("");
        print(String.format("Running tests for %s. Found: %d", name, testSize));
        print("");
        totalTestCount = testSize;
        runningTestIndex = 1;
    }

    @Override
    public long printStartTest(AssertTest assertion, Column column) {
        String format = String.format("%s of %s, RUNNING test ('%s') on column: '%s'", runningTestIndex,
                totalTestCount, assertion.getOperator(), column.getName());
        print(append(format, " "));
        return System.currentTimeMillis();
    }

    @Override
    public void printEndTest(AssertTest assertion, Column column, long startTime, boolean pass, String result) {
        long diff = System.currentTimeMillis() - startTime;
        String format1 = String.format("%s of %s, FINISHED test on column: '%s' (expected: '%s' - actual: '%s')  ",
                runningTestIndex, totalTestCount, column.getName(), assertion.getExpected(), result);
        String format2 = String.format("[%s in %ss]", pass ?
                "\u001b[0;38;2;0;255;0mPASS\u001b[m" : "\u001b[0;38;2;252;16;13mFAIL\u001b[m", diff / 1000f);
        print(append(format1, ".") + " " + format2);
        runningTestIndex = runningTestIndex + 1;
    }

    @Override
    public void endTestForDatabase() {
        print("");
    }

    public void print(String msg) {
        System.out.printf("%s  %s%n", SIMPLE_DATE_FORMAT.format(new Date()), msg);
    }

    private String append(String value, String appenderValue) {
        int filler = 100 - value.length();
        return value + String.valueOf(appenderValue).repeat(Math.max(0, filler));
    }

}
