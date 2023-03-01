package com.adaptivescale.rosetta.ddl.test;

import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.ddl.change.ChangeHandler;
import com.adaptivescale.rosetta.ddl.change.ChangeHandlerImplementation;
import com.adaptivescale.rosetta.ddl.change.DefaultChangeFinder;
import com.adaptivescale.rosetta.ddl.change.model.Change;
import com.adaptivescale.rosetta.ddl.targets.spanner.SpannerDDLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SpannerDDLTest {

    private static final Path resourceDirectory = Paths.get("src", "test", "resources", "ddl", "spanner");

    @Test
    public void createDB() throws IOException {
        String ddl = generateDDL("clean_database");
        Assertions.assertEquals("CREATE TABLE Singers(SingerId INT64 NOT NULL , FirstName STRING(1024), LastName STRING(1024), SingerInfo BYTES(MAX)) PRIMARY KEY (SingerId);\r" +
                "\r" +
                "CREATE TABLE Albums(SingerId INT64 NOT NULL , AlbumId INT64 NOT NULL , AlbumTitle STRING(MAX)) PRIMARY KEY (SingerId, AlbumId),\r" +
                "INTERLEAVE IN PARENT Singers ON DELETE CASCADE;", ddl);
    }

    @Test
    public void addTable() throws IOException {
        String ddl = generateDDL("add_table");
        Assertions.assertEquals("CREATE TABLE SingersNew(SingerId INT64 NOT NULL , FirstName STRING(1024), LastName STRING(1024), SingerInfo BYTES(MAX)) PRIMARY KEY (SingerId);", ddl);
    }

    @Test
    public void addInterleaveTable() throws IOException {
        String ddl = generateDDL("add_interleave_table");
        Assertions.assertEquals("CREATE TABLE AlbumsNew(SingerId INT64 NOT NULL , AlbumId INT64 NOT NULL , AlbumTitle STRING(MAX)) PRIMARY KEY (SingerId, AlbumId),\r" +
                "INTERLEAVE IN PARENT Singers ON DELETE CASCADE;", ddl);
    }

    @Test
    public void addColumn() throws IOException {
        String ddl = generateDDL("add_column");
        Assertions.assertEquals("ALTER TABLE Albums ADD COLUMN NewColumn INT64 NOT NULL ;", ddl);
    }

    @Test
    public void dropColumn() throws IOException {
        String ddl = generateDDL("drop_column");
        Assertions.assertEquals("ALTER TABLE Singers DROP COLUMN FirstName;", ddl);
    }

    @Test
    public void alterColumnDataType() throws IOException {
        String ddl = generateDDL("alter_column_data_type");
        Assertions.assertEquals("ALTER TABLE Singers ALTER COLUMN SingerInfo STRING(MAX);", ddl);
    }

    private String generateDDL(String testType) throws IOException {
        Database actual = Utils.getDatabase(resourceDirectory.resolve(testType), "actual_model.yaml");
        Database expected = Utils.getDatabase(resourceDirectory.resolve(testType), "expected_model.yaml");
        List<Change<?>> changes = new DefaultChangeFinder().findChanges(expected, actual);
        ChangeHandler handler = new ChangeHandlerImplementation(new SpannerDDLGenerator(), null);
        return handler.createDDLForChanges(changes);
    }
}
