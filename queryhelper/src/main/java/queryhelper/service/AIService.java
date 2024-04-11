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

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class AIService {
    private final static String AI_MODEL = "gpt-3.5-turbo";

    public static GenericResponse generateQuery(String userQueryRequest, String apiKey, String aiModel, String databaseDDL, Connection source, Integer showRowLimit, Path dataDirectory) {

        Gson gson = new Gson();
        GenericResponse response = new GenericResponse();
        QueryDataResponse data = new QueryDataResponse();
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setQuery(userQueryRequest);

        String query;
        String aiOutputStr;
        String prompt;

        OpenAiChatModel.OpenAiChatModelBuilder model = OpenAiChatModel
                .builder()
                .temperature(0.1)
                .apiKey(apiKey)
                .modelName(AI_MODEL);

        if (aiModel != null && !aiModel.isEmpty()) {
            model.modelName(aiModel);
        }

        prompt = PromptUtils.queryPrompt(queryRequest, databaseDDL, source);

        try {  // Check if we have a properly set API key & that openAI services aren't down
            aiOutputStr = model.build().generate(prompt);
            QueryRequest aiOutputObj = gson.fromJson(aiOutputStr, QueryRequest.class);
            query = aiOutputObj.getQuery();
        } catch (JsonSyntaxException e) {
            return ErrorUtils.invalidResponseError(e);
        } catch (Exception e) {
            return ErrorUtils.openAIError(e);
        }

        boolean selectStatement = isSelectStatement(query);
        if (!selectStatement) {
            GenericResponse errorResponse = new GenericResponse();
            errorResponse.setMessage("Generated query, execute on your own will: " + aiOutputStr);
            errorResponse.setStatusCode(200);
        }

        List<Map<String, Object>> records = executeQueryAndGetRecords(query, source, showRowLimit);
        data.setRecords(records);

        response.setData(data);
        response.setStatusCode(200);


        QueryDataResponse queryDataResponse = (QueryDataResponse) response.getData();
        String csvFile = createCSVFile(queryDataResponse, queryRequest.getQuery(), dataDirectory);

        response.setMessage(
                aiOutputStr + "\n" +
                        "Total rows: " + data.getRecords().size() + "\n" +
                        "Your response is saved to a CSV file named '" + csvFile + "'!"
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

    private static String createCSVFile(QueryDataResponse queryDataResponse, String csvFileName, Path dataDirectory) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = csvFileName.replaceAll("\\s+", "_") + "_" + timestamp + ".csv";
            Path csvFilePath = dataDirectory.resolve(fileName);

            FileUtils.convertToCSV(csvFilePath.toString(), queryDataResponse.getRecords());

            return csvFilePath.toString();
        } catch (Exception e) {
            GenericResponse genericResponse = ErrorUtils.csvFileError(e);
            throw new RuntimeException(genericResponse.getMessage());
        }
    }
}