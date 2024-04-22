package com.adaptivescale.rosetta.translation;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.translator.DefaultTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTranslatorTest {

    private DefaultTranslator subject;

    @ParameterizedTest
    @EnumSource(TranslationScenario.class)
    public void translateSourceToTargetTest(TranslationScenario translationScenario) {
        subject = new DefaultTranslator(translationScenario.getSourceDatabaseType(), translationScenario.getTargetDatabaseType());
        Database source = translationScenario.toSourceDatabase();
        Database result = subject.translate(source);

        for (Table table : result.getTables()) {
            int tableIndex = result.getTables().stream().collect(Collectors.toList()).indexOf(table);
            for (Column column : table.getColumns()) {
                int columnIndex = table.getColumns().stream().collect(Collectors.toList()).indexOf(column);
                Assertions.assertEquals(
                        column.getTypeName(),
                        translationScenario.getTables().get(tableIndex).getColumns().get(columnIndex).getTranslatedTypeName()
                );
            }
        }

        Assertions.assertEquals(result.getTables().size(), 1);
    }


    enum TranslationScenario {
        SQLSERVER_TO_SNOWFLAKE("sqlserver", "snowflake", Arrays.asList(FakeTable.SQLSERVER_TABLE_TO_SNOWFLAKE)),
        POSTGRES_TO_KINETICA("postgres", "kinetica", Arrays.asList(FakeTable.POSTGRES_TABLE_TO_KINETICA));

        private String sourceDatabaseType;
        private String targetDatabaseType;
        private List<FakeTable> tables;

        TranslationScenario(String sourceDatabaseType, String targetDatabaseType, List<FakeTable> tables) {
            this.sourceDatabaseType = sourceDatabaseType;
            this.targetDatabaseType = targetDatabaseType;
            this.tables = tables;
        }

        public String getSourceDatabaseType() {
            return sourceDatabaseType;
        }

        public String getTargetDatabaseType() {
            return targetDatabaseType;
        }

        public List<FakeTable> getTables() {
            return tables;
        }

        public Database toSourceDatabase() {
            Database database = new Database();
            database.setDatabaseType(this.getSourceDatabaseType());
            database.setTables(toTables());

            return database;
        }

        public List<Table> toTables() {
            List<Table> resultTables = new ArrayList<>();
            for (FakeTable table: this.tables) {
                Table table1 = new Table();
                table1.setName(table.getTableName());
                table1.setColumns(toColumns(table));
                resultTables.add(table1);
            }

            return resultTables;
        }

        public List<Column> toColumns(FakeTable table) {
            List<Column> resultColumns = new ArrayList<>();
            for (FakeColumn column: table.getColumns()) {
                Column column1 = new Column();
                column1.setName(column.getName());
                column1.setTypeName(column.getTypeName());
                column1.setNullable(column.getNullable());
                column1.setPrimaryKey(column.getPrimaryKey());
                column1.setColumnDisplaySize(column.getColumnDisplaySize());

                resultColumns.add(column1);
            }
            return resultColumns;
        }
    }

    enum FakeTable {

        SQLSERVER_TABLE_TO_SNOWFLAKE("test", Arrays.asList(
                FakeColumn.SQLSERVER_COLUMN_INT_TO_SNOWFLAKE,
                FakeColumn.SQLSERVER_COLUMN_TEXT_TO_SNOWFLAKE
            )
        ),
        POSTGRES_TABLE_TO_KINETICA("test", Arrays.asList(
                FakeColumn.POSTGRES_COLUMN_INT_TO_KINETICA,
                FakeColumn.POSTGRES_COLUMN_TEXT_TO_KINETICA
            )
        );

        private String tableName;
        private List<FakeColumn> columns;

        FakeTable(String tableName, List<FakeColumn> columns) {
            this.tableName = tableName;
            this.columns = columns;
        }

        public String getTableName() {
            return tableName;
        }

        public List<FakeColumn> getColumns() {
            return columns;
        }
    }

    enum FakeColumn {
        SQLSERVER_COLUMN_INT_TO_SNOWFLAKE("test", "int identity", false, true, 10, "int"),
        SQLSERVER_COLUMN_TEXT_TO_SNOWFLAKE("tes2", "text", false, false, 10, "text"),
        POSTGRES_COLUMN_INT_TO_KINETICA("test", "integer", false, false, 10, "integer"),
        POSTGRES_COLUMN_TEXT_TO_KINETICA("test2", "text", false, false, 10, "string");

        private String name;
        private String typeName;
        private Boolean nullable;
        private Boolean primaryKey;
        private Integer columnDisplaySize;
        private String translatedTypeName;

        FakeColumn(String name, String typeName, Boolean nullable, Boolean primaryKey, Integer columnDisplaySize, String translatedTypeName) {
            this.name = name;
            this.typeName = typeName;
            this.nullable = nullable;
            this.primaryKey = primaryKey;
            this.columnDisplaySize = columnDisplaySize;
            this.translatedTypeName = translatedTypeName;
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        public Boolean getNullable() {
            return nullable;
        }

        public Boolean getPrimaryKey() {
            return primaryKey;
        }

        public Integer getColumnDisplaySize() {
            return columnDisplaySize;
        }

        public String getTranslatedTypeName() {
            return translatedTypeName;
        }
    }
}

