package com.adaptivescale.rosetta.diff;

import com.adaptivescale.rosetta.common.models.Database;

import java.util.List;

public class DiffFactory {

    public static Diff<List<String>,Database, Database> diff(){
        return new DefaultTester();
    }
}
