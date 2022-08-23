package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.change.ChangeFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ChangeListTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "changes");

    @Test
    public void noChanges() throws IOException {
        List<Change<?>> changes = findChanges("no_changes");
        Assertions.assertEquals(0,changes.size());
    }

    @Test
    public void addTable() throws IOException {
        List<Change<?>> changes = findChanges("add_table");
        Assertions.assertEquals(1,changes.size());
        Change<?> change = changes.get(0);
        Assertions.assertEquals(Change.Status.ADD,change.getStatus());
    }

    @Test
    public void dropTable() throws IOException {
        List<Change<?>> changes = findChanges("drop_table");
        Assertions.assertEquals(1,changes.size());
        Change<?> change =  changes.get(0);
        Assertions.assertEquals(Change.Status.DROP,change.getStatus());
    }

    @Test
    public void addColumn() throws IOException {
        List<Change<?>> changes = findChanges("add_column");
        Assertions.assertEquals(1,changes.size());
        Change<?> change =  changes.get(0);
        Assertions.assertEquals(Change.Status.ADD,change.getStatus());
    }

    @Test
    public void dropColumn() throws IOException {
        List<Change<?>> changes = findChanges("drop_column");
        Assertions.assertEquals(1,changes.size());
        Change<?> change = changes.get(0);
        Assertions.assertEquals(Change.Status.DROP,change.getStatus());
    }

    @Test
    public void dataTypeOnColumnAlter() throws Exception {
        List<Change<?>> changes = findChanges("data_type_column_change");
        Assertions.assertEquals(1,changes.size());
        Change<?> change = changes.get(0);
        Assertions.assertEquals(Change.Status.ALTER,change.getStatus());
    }

    @Test
    public void foreignKeyAddAlterDrop() throws IOException {
        List<Change<?>> changes = findChanges("foreign_keys");
        Assertions.assertEquals(3, changes.size());
        Assertions.assertEquals(1, (int) changes.stream().filter(change -> change.getStatus().equals(Change.Status.ADD)).count());
        Assertions.assertEquals(1, (int) changes.stream().filter(change -> change.getStatus().equals(Change.Status.DROP)).count());
        Assertions.assertEquals(1, (int) changes.stream().filter(change -> change.getStatus().equals(Change.Status.ALTER)).count());
    }


    private List<Change<?>> findChanges(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        return new ChangeFinder().findChanges(expected, actual);
    }
}
