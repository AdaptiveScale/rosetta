package queryhelper.service;

import com.adaptivescale.rosetta.common.models.input.Connection;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.langchain4j.model.openai.OpenAiChatModel;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Select;
import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryDataResponse;
import queryhelper.pojo.QueryRequest;
import queryhelper.utils.ErrorUtils;
import queryhelper.utils.PromptUtils;

import java.nio.file.Path;
import java.util.*;

import static queryhelper.utils.FileUtils.createCSVFile;
import static queryhelper.utils.FileUtils.generateTablePreview;
import static queryhelper.utils.QueryUtils.executeQueryAndGetRecords;


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
}