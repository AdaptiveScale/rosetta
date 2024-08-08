package queryhelper.service;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.langchain4j.model.openai.OpenAiChatModel;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryDataResponse;
import queryhelper.pojo.QueryRequest;
import queryhelper.utils.ErrorUtils;
import queryhelper.utils.FileUtils;
import queryhelper.utils.PromptUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;


public class AIService {
    private final static String AI_MODEL = "gpt-3.5-turbo";

    public static GenericResponse generateQuery(String userQueryRequest, String apiKey, String aiModel, String databaseDDL, Connection source, Integer showRowLimit, Path dataDirectory, Path outputFileName) {

        GenericResponse response = new GenericResponse();
        QueryDataResponse data = new QueryDataResponse();
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setQuery(userQueryRequest);

        String query;
        query = generateAIOutput(apiKey, aiModel, queryRequest, source, databaseDDL);

        boolean selectStatement = isSelectStatement(query);
        if (!selectStatement) {
            GenericResponse errorResponse = new GenericResponse();
            errorResponse.setMessage("Generated query, execute on your own will: " + query);
            errorResponse.setStatusCode(200);
        }

        List<Map<String, Object>> records = executeQueryAndGetRecords(query, source, showRowLimit);
        data.setRecords(records);

        response.setData(data);
        response.setStatusCode(200);


        QueryDataResponse queryDataResponse = (QueryDataResponse) response.getData();
        String csvFile = createCSVFile(queryDataResponse, queryRequest.getQuery(), dataDirectory, outputFileName);

        String table = generateTablePreview(csvFile, 15);

        response.setMessage(
                query + "\n" +
                        "Your response is saved to a CSV file named '" + csvFile + "'!" + "\n" +
                        "Table Output:" +"\n" +
                        table +
                        "..." + "\n" +
                        "Total rows: " + data.getRecords().size()
        );

        return response;
    }

    private static List<Map<String, Object>> executeQueryAndGetRecords(String query, Connection source, Integer showRowLimit) {
        try {
            DriverManagerDriverProvider driverManagerDriverProvider = new DriverManagerDriverProvider();
            Driver driver = driverManagerDriverProvider.getDriver(source);
            Properties properties = JDBCUtils.setJDBCAuth(source);
            java.sql.Connection jdbcConnection = driver.connect(source.getUrl(), properties);
            Statement statement = jdbcConnection.createStatement();
            statement.setMaxRows(showRowLimit);
            List<Map<String, Object>> select = QueryHelper.select(statement, query);
            return select;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSelectStatement(String query) {
        boolean isSelectStatement = true;
        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(query);
            if (!(statement instanceof Select)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return isSelectStatement;
    }

    private static String createCSVFile(QueryDataResponse queryDataResponse, String csvFileName, Path dataDirectory, Path outputFileName) {
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

    public static String generateAIOutput(String apiKey, String aiModel, QueryRequest queryRequest, Connection source, String databaseDDL) {
        Gson gson = new Gson();
        String aiOutputStr;
        String query;

        OpenAiChatModel.OpenAiChatModelBuilder model = OpenAiChatModel
                .builder()
                .temperature(0.1)
                .apiKey(apiKey)
                .modelName(AI_MODEL);

        if (aiModel != null && !aiModel.isEmpty()) {
            model.modelName(aiModel);
        }

        String prompt = PromptUtils.queryPrompt(queryRequest, databaseDDL, source);

        try {
            aiOutputStr = model.build().generate(prompt);
            QueryRequest aiOutputObj = gson.fromJson(aiOutputStr, QueryRequest.class);
            query = aiOutputObj.getQuery();
        } catch (JsonSyntaxException e) {
            GenericResponse genericResponse = ErrorUtils.invalidResponseError(e);
            throw new RuntimeException(genericResponse.getMessage());
        } catch (Exception e) {
            GenericResponse genericResponse = ErrorUtils.openAIError(e);
            throw new RuntimeException(genericResponse.getMessage());
        }

        return query;
    }
    private static String generateTablePreview(String csvFile, int rowLimit) {
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

    private static String buildRowSeparator(int[] columnWidths) {
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