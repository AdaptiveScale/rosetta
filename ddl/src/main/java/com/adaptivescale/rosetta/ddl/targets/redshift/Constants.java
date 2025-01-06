package com.adaptivescale.rosetta.ddl.targets.redshift;

import java.util.List;

public class Constants {
    public static String DEFAULT_WRAPPER = "\"";

    public static List<String> PRECISION_TYPES = List.of(
            "varchar",
            "decimal",
            "char"
    );

    public static List<Integer> PRECISION_DEFAULTS = List.of(
            65535,
            18,
            1
    );

}
