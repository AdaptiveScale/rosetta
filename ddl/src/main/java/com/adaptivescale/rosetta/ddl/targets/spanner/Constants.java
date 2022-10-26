package com.adaptivescale.rosetta.ddl.targets.spanner;

import java.util.List;

public class Constants {
    public static String DEFAULT_WRAPPER = "";
    public static List<String> PRECISION_TYPES = List.of(
            "STRING",
            "BYTES"
    );

    public static List<Integer> PRECISION_DEFAULTS = List.of(
            2147483647,
            0
    );
}
