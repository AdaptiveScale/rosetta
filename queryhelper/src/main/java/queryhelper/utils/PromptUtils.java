package queryhelper.utils;

import com.adaptivescale.rosetta.common.models.input.Connection;
import queryhelper.pojo.QueryRequest;


public class PromptUtils {
    public static String queryPrompt(QueryRequest queryRequest, String databaseDDL, Connection source) {
        String query = queryRequest.getQuery();

        String outputFormat = FileUtils.readJsonFile();

        return "You are a system that generates and outputs " + source.getDbType() + " SQL queries.\n" +
                "The following is the DDL of the database:\n\n" +
                "START OF DDL\n" + databaseDDL + "\nEND OF DDL" +
                "\n\nI want you to generate a SQL query based on the following description: " +
                query +
                " Respond only by giving me the SQL code with no other accompanying text in the following format:\n" +
                outputFormat;
    }

    public static String dbtBusinessLayerPrompt(String modelContents, String userPrompt) {
        if (userPrompt == null || userPrompt.isEmpty()) {
            userPrompt = "Generate exactly one business model from these incremental models.";
        }

        String yamlOutputFormat =
                "  - fileName: {fileName}.sql  # Ensure the filename ends with .sql\n" +
                        "    content: |\n";

        return "You are an AI system that must generate and output ONLY ONE business layer DBT model in YAML format. " +
                "DO NOT output anything else, including explanations or surrounding text.\n\n" +
                "Your response MUST strictly follow the format below. The filename MUST always end in .sql.\n" +
                "\n\n" + userPrompt + "\n" +
                modelContents +
                "\n\nDO NOT include the ```yaml block at the beginning or end. Only respond with valid YAML in the following format:\n" +
                yamlOutputFormat +
                "\nIMPORTANT: The {fileName} placeholder must always be replaced with an actual filename ending in '.sql'.";
    }

}
