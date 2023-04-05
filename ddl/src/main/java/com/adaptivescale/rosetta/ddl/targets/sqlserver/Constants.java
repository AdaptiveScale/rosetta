package com.adaptivescale.rosetta.ddl.targets.sqlserver;

import java.util.List;

//TODO: Check the Constants
public class Constants {

    public static String DEFAULT_WRAPPER = "\"";
    public static List<String> PRECISION_TYPES = List.of(
            "varchar",
            "decimal",
            "float"
    );

    public static List<Integer> PRECISION_DEFAULTS = List.of(
            2147483647,
            0
    );
}
