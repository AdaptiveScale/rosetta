package com.adaptivescale.rosetta.test.assertion;

import com.adaptivescale.rosetta.common.models.AssertTest;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Database;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adaptivescale.rosetta.common.models.test.Tests;
import com.adaptivescale.rosetta.test.assertion.output.ConsoleOutput;
import com.adaptivescale.rosetta.test.assertion.output.Output;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DefaultAssertTestEngine implements AssertTestEngine {

    private final AssertionSqlGenerator sqlGenerator;
    private final SqlExecution sqlExecution;
    private final Output output;

    public DefaultAssertTestEngine(AssertionSqlGenerator sqlGenerator, SqlExecution sqlExecution) {
        this.sqlGenerator = sqlGenerator;
        this.sqlExecution = sqlExecution;
        output = new ConsoleOutput();
    }

    @Override
    public void run(Connection connection, Database database) {
        List<AssertTest> collect = database
                .getTables()
                .stream()
                .flatMap(table1 -> table1.getColumns().stream())
                .filter(column -> column.getTests() != null && column.getTests().getAssertion() != null && column.getTests().getAssertion().size() > 0)
                .flatMap(column -> column.getTests().getAssertion().stream()).collect(Collectors.toList());

        output.startTestForDatabase(connection.getName(), collect.size());

        for (Table table : database.getTables()) {
            Collection<Column> columns = table.getColumns();
            for (Column column : columns) {
                Tests tests = column.getTests();
                if (tests == null) {
                    continue;
                }
                Collection<AssertTest> assertions = tests.getAssertion();
                if (assertions == null || assertions.isEmpty()) {
                    continue;
                }
                for (AssertTest assertion : assertions) {
                    long startTime = output.printStartTest(assertion, column);
                    String sql = sqlGenerator.generateSql(connection, table, column, assertion);
                    String result = sqlExecution.execute(sql);
                    boolean pass = Objects.equals(assertion.getExpected(), result);
                    output.printEndTest(assertion, column, startTime, pass, result);
                }
            }
        }

        output.endTestForDatabase();
    }
}
