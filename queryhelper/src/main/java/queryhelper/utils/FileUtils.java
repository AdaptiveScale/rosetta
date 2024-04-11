package queryhelper.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

}
