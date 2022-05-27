package com.adataptivescale.rosetta.source.common;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryHelper {
    static DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd");

    public static List<Map<String, Object>> select(Statement stmt, String query) throws SQLException {
        ResultSet resultSet = stmt.executeQuery(query);
        List<Map<String, Object>> records = mapRecords(resultSet);

//        resultSet.close();

        return records;
    }

    public static List<Map<String, Object>> mapRecords(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            records.add(mapRecord(resultSet, metaData));
        }

        return records;
    }

    public static Map<String, Object> mapRecord(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        Map<String, Object> record = new HashMap<>();

        for (int c = 1; c <= metaData.getColumnCount(); c++) {
            String columnType = metaData.getColumnTypeName(c);
            String columnName = formatPropertyName(metaData.getColumnName(c));
            Object value = resultSet.getObject(c);

            if (columnType.equals("DATE")) {
                value = DATE_FORMAT.format(value);
            }
            record.put(columnName, value);
        }
        return record;
    }


    public static List<Map<String, Object>> mapRecordsUsingColumnLabel(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            records.add(mapRecordUsingColumnLabel(resultSet, metaData));
        }
//        resultSet.close();

        return records;
    }

    public static List<Map<String, Object>> mapRecordsUsingColumnLabel(ResultSet resultSet, int limit) throws SQLException {
        List<Map<String, Object>> records = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int i = limit; i > 0 && resultSet.next(); i--) {
            records.add(mapRecordUsingColumnLabel(resultSet, metaData));
        }
//        resultSet.close();
        return records;
    }


    public static Map<String, Object> mapRecordUsingColumnLabel(ResultSet resultSet, ResultSetMetaData metaData) throws SQLException {
        Map<String, Object> record = new HashMap<>();

        for (int c = 1; c <= metaData.getColumnCount(); c++) {
            String columnType = metaData.getColumnTypeName(c);
            String columnLabel = formatPropertyName(metaData.getColumnLabel(c));
            Object value = resultSet.getObject(c);

            if (columnType.equals("DATE")) {
                value = DATE_FORMAT.format(value);
            }

            record.put(columnLabel, value);
        }

        return record;
    }


    private static String formatPropertyName(String property) {
        return new PropertyNamingStrategies.LowerCaseStrategy().translate(property);
    }

    public static Map<String, Object> getColumnsForResultSet(ResultSet resultSet) throws SQLException {
        Map<String, Object> record = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        for (int c = 1; c <= metaData.getColumnCount(); c++) {
            String columnLabel = formatPropertyName(metaData.getColumnLabel(c));

            record.put(columnLabel, columnLabel);
        }

        return record;
    }
}