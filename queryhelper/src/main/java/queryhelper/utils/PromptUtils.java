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
            userPrompt = "Generate me one business model from these incremental models.";
        }

        String yamlOutputFormat =
                    "  - fileName: {fileName}.sql\n" +
                    "    content: |\n";

        return "You are a system that generates and outputs only ONE business layer DBT model.\n" +
                "\n\n "+ userPrompt + "\n" +
                modelContents +
                "\n\n Don't include the ```yaml at the beginning. Respond only by giving me the YAML output in the following format:\n" +
                yamlOutputFormat;
    }

}
