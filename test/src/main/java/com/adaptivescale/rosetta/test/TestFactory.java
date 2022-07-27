package com.adaptivescale.rosetta.test;

import com.adaptivescale.rosetta.common.models.Database;

import java.util.List;

public class TestFactory {

    public static Tester<List<String>,Database, Database> tester(){
        return new DefaultTester();
    }
}
