package queryhelper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonSyntaxException;
import dev.langchain4j.model.openai.OpenAiChatModel;
import queryhelper.pojo.DbtOutput;
import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryRequest;
import queryhelper.utils.ErrorUtils;
import queryhelper.utils.PromptUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DbtAIService {
    private final static String AI_MODEL = "gpt-3.5-turbo";

    public static GenericResponse generateBusinessModels(String apiKey, String aiModel, Path outputDir, String modelContents) throws JsonProcessingException {

        GenericResponse response = new GenericResponse();

        String outputAi = generateAIOutput(apiKey,aiModel,modelContents);
        List<DbtOutput> output = new ObjectMapper(new YAMLFactory()).readValue(outputAi, new TypeReference<List<DbtOutput>>(){});


        try {
            for (DbtOutput modelFile : output) {
                Path filePath = outputDir.resolve(modelFile.getFileName());
                Files.write(filePath, modelFile.getContent().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            response.setMessage("Error: " + e.getMessage());
            response.setStatusCode(500);
            return response;
        }

        response.setMessage("Successfully generated business models.");
        response.setStatusCode(200);
        return response;
    }

    public static String generateAIOutput(String apiKey, String aiModel, String combinedModelContents) {
        String aiOutputStr;

        OpenAiChatModel.OpenAiChatModelBuilder model = OpenAiChatModel
                .builder()
                .temperature(0.1)
                .apiKey(apiKey)
                .modelName(AI_MODEL);

        if (aiModel != null && !aiModel.isEmpty()) {
            model.modelName(aiModel);
        }

        String prompt = PromptUtils.dbtBusinessLayerPrompt(combinedModelContents);

        try {
            aiOutputStr = model.build().generate(prompt);
        } catch (JsonSyntaxException e) {
            GenericResponse genericResponse = ErrorUtils.invalidResponseError(e);
            throw new RuntimeException(genericResponse.getMessage());
        } catch (Exception e) {
            GenericResponse genericResponse = ErrorUtils.openAIError(e);
            throw new RuntimeException(genericResponse.getMessage());
        }

        return aiOutputStr;
    }
}