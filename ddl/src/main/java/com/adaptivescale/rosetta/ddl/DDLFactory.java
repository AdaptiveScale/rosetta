package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.JDBCDriverProvider;
import com.adaptivescale.rosetta.common.helpers.ModuleLoader;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.*;
import com.adaptivescale.rosetta.ddl.change.comparator.*;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.executor.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Optional;

public class DDLFactory {

    public static DDL ddlForDatabaseType(String databaseType) {
        Optional<Class<?>> ddlGenerator = ModuleLoader.loadModuleByAnnotationClassValues(
                DDLFactory.class.getPackageName(), RosettaModuleTypes.DDL_GENERATOR, databaseType);
        if(ddlGenerator.isEmpty()) {
            throw new RuntimeException("DDL not supported for database type: " + databaseType);
        }
        try {
            return (DDL) ddlGenerator.get().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static DDLExecutor executor(Connection connection, JDBCDriverProvider driverProvider) {
        String dbType = connection.getDbType();
        Optional<Class<?>> ddlGenerator = ModuleLoader.loadModuleByAnnotationClassValues(
                DDLFactory.class.getPackageName(), RosettaModuleTypes.DDL_EXECUTOR, dbType);
        if(ddlGenerator.isEmpty()) {
            throw new RuntimeException("DDL not supported for database type: " + dbType);
        }
        try {
            return (DDLExecutor) ddlGenerator.get()
                    .getDeclaredConstructor(
                            Connection.class,
                            JDBCDriverProvider.class
                    ).newInstance(
                            connection,
                            driverProvider
                    );
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangeHandler changeHandler(String databaseType) {
        DDL ddl = ddlForDatabaseType(databaseType);
        Comparator<Change<?>> changeComparator = changesSortComparatorForDatabase(databaseType);
        return new ChangeHandlerImplementation(ddl, changeComparator);
    }

    private static Comparator<Change<?>> changesSortComparatorForDatabase(String databaseType) {
        Optional<Class<?>> ddlGenerator = ModuleLoader.loadModuleByAnnotationClassValues(
                DDLFactory.class.getPackageName(), RosettaModuleTypes.CHANGE_COMPARATOR, databaseType);
        if(ddlGenerator.isEmpty()) {
            throw new RuntimeException("DDL not supported for database type: " + databaseType);
        }
        try {
            return ( Comparator<Change<?>>) ddlGenerator.get().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangeFinder changeFinderForDatabaseType(String databaseType) {
        Optional<Class<?>> ddlGenerator = ModuleLoader.loadModuleByAnnotationClassValues(
                DDLFactory.class.getPackageName(), RosettaModuleTypes.CHANGE_FINDER, databaseType);
        if(ddlGenerator.isEmpty()) {
            return new DefaultChangeFinder();
        }
        try {
            return ( ChangeFinder) ddlGenerator.get().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


}
