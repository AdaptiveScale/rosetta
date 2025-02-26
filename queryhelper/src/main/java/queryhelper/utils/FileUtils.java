package queryhelper.utils;

import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryDataResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {


    public static String readJsonFile() {
        try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream("static/output_format.json");
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createCSVFile(QueryDataResponse queryDataResponse, String csvFileName, Path dataDirectory, Path outputFileName) {
        try {
            if (outputFileName == null) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fileName = csvFileName.replaceAll("\\s+", "_") + "_" + timestamp + ".csv";
                Path csvFilePath = dataDirectory.resolve(fileName);
                FileUtils.convertToCSV(csvFilePath.toString(), queryDataResponse.getRecords());

                return csvFilePath.toString();
            }

            Path csvFilePath = dataDirectory.resolve(outputFileName.toString());
            FileUtils.convertToCSV(csvFilePath.toString(), queryDataResponse.getRecords());
            return csvFilePath.toString();

        } catch (Exception e) {
            GenericResponse genericResponse = ErrorUtils.csvFileError(e);
            throw new RuntimeException(genericResponse.getMessage());
        }
    }


    public static void convertToCSV(String fileName, List<Map<String, Object>> list) {
        try (FileWriter csvWriter = new FileWriter(fileName)) {
            if (!list.isEmpty()) {
                Set<String> headers = list.get(0).keySet();
                StringBuilder csvContent = new StringBuilder();
                csvContent.append(String.join(",", headers)).append("\n");

                for (Map<String, Object> map : list) {
                    for (String header : headers) {
                        csvContent.append(map.getOrDefault(header, "")).append(",");
                    }
                    csvContent.setLength(csvContent.length() - 1);
                    csvContent.append("\n");
                }
                csvWriter.write(csvContent.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateTablePreview(String csvFile, int rowLimit) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int rowCount = 0;
            while ((line = reader.readLine()) != null && rowCount < rowLimit) {
                String[] columns = line.split(",");
                rows.add(columns);
                rowCount++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }

        if (rows.isEmpty()) {
            return "No data available to display.";
        }
        int maxColumns = rows.stream().mapToInt(row -> row.length).max().orElse(0);
        int[] columnWidths = new int[maxColumns];
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                columnWidths[i] = Math.max(columnWidths[i], row[i].length());
            }
        }
        StringBuilder table = new StringBuilder();
        String rowSeparator = buildRowSeparator(columnWidths);

        table.append(rowSeparator);
        for (String[] row : rows) {
            table.append("|");
            for (int i = 0; i < maxColumns; i++) {
                String cell = (i < row.length) ? row[i] : "";
                table.append(" ").append(String.format("%-" + columnWidths[i] + "s", cell)).append(" |");
            }
            table.append("\n").append(rowSeparator);
        }

        return table.toString();
    }

    public static String buildRowSeparator(int[] columnWidths) {
        StringBuilder separator = new StringBuilder("+");
        for (int width : columnWidths) {
            for (int i = 0; i < width + 2; i++) {
                separator.append("-");
            }
            separator.append("+");
        }
        separator.append("\n");
        return separator.toString();
    }

}
