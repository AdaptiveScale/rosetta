package queryhelper.service;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.common.QueryHelper;
import com.google.gson.Gson;
import dev.langchain4j.model.openai.OpenAiChatModel;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryDataResponse;
import queryhelper.pojo.QueryRequest;
import queryhelper.utils.ErrorUtils;
import queryhelper.utils.FileUtils;
import queryhelper.utils.PromptUtils;

import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class AIService {
    private final static String AI_MODEL = "gpt-3.5-turbo";
    public static GenericResponse generateQuery(QueryRequest queryRequest, String apiKey, String aiModel, String databaseDDL, Connection source, String csvFileName, Integer showRowLimit) {

        Gson gson = new Gson();
        GenericResponse response = new GenericResponse();
        QueryDataResponse data = new QueryDataResponse();

        long startTime;
        long endTime;
        double elapsedTime;
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

        try { // Check if the file with given name exists
            prompt = PromptUtils.queryPrompt(queryRequest, databaseDDL, source);
        } catch (Exception e) {
            return ErrorUtils.fileError(e);
        }

        try {  // Check if we have a properly set API key & that openAI services aren't down
            startTime = System.currentTimeMillis();
            aiOutputStr = model.build().generate(prompt);
            endTime = System.currentTimeMillis();
            elapsedTime = (endTime - startTime) / 1000.0;
        } catch (Exception e) {
            return ErrorUtils.openAIError(e);
        }

        try {
            QueryRequest aiOutputObj = gson.fromJson(aiOutputStr, QueryRequest.class);
            query = aiOutputObj.getQuery();
        } catch (Exception e) {
            return ErrorUtils.invalidResponseError(e);
        }

        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(query);
            if (!(statement instanceof Select)) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            GenericResponse errorResponse = new GenericResponse();
            errorResponse.setMessage("Generated query, execute on your own will: " + aiOutputStr);
            errorResponse.setStatusCode(200);
            return errorResponse;
        }

        try {
            DriverManagerDriverProvider driverManagerDriverProvider = new DriverManagerDriverProvider();
            Driver driver = driverManagerDriverProvider.getDriver(source);
            Properties properties = JDBCUtils.setJDBCAuth(source);
            java.sql.Connection jdbcConnection = driver.connect(source.getUrl(), properties);
            Statement statement = jdbcConnection.createStatement();
            statement.setMaxRows(showRowLimit);
            List<Map<String, Object>> select = QueryHelper.select(statement, query);

            data.setRecords(select);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setMessage(
                aiOutputStr + "\n" +
                        "Total rows: " + data.getRecords().size() + "\n" +
                        "Your response is saved to a CSV file named '" + csvFileName + "'!"
        );

        response.setData(data);
        response.setStatusCode(200);

        QueryDataResponse queryDataResponse = (QueryDataResponse) response.getData();
        try {
            FileUtils.convertToCSV(csvFileName, queryDataResponse.getRecords());
        } catch (Exception e) {
            return ErrorUtils.csvFileError(e);
        }

        return response;
    }
}