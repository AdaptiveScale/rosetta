package com.adaptivescale.rosetta.diff;

import com.adaptivescale.rosetta.common.helpers.ModuleLoader;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

public class DiffFactory {

    public static Diff<List<String>,Database, Database> diff(String databaseType) {

        Optional<Class<?>> diffTester = ModuleLoader.loadModuleByAnnotationClassValues(
                DiffFactory.class.getPackageName(), RosettaModuleTypes.DIFF_TESTER, databaseType);

        if (diffTester.isEmpty()) {
            return new DefaultTester();
        }

        try {
            return (Diff<List<String>, Database, Database>) diffTester.get().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
