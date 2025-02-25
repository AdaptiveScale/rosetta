package queryhelper.service;

import com.adaptivescale.rosetta.common.models.input.Connection;
import queryhelper.pojo.GenericResponse;
import queryhelper.pojo.QueryDataResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;


import static queryhelper.utils.FileUtils.createCSVFile;
import static queryhelper.utils.FileUtils.generateTablePreview;
import static queryhelper.utils.QueryUtils.executeQueryAndGetRecords;

public class QueryService {

    public static GenericResponse executeQuery(String query, Connection source, Integer showRowLimit, Path dataDirectory, Path outputFileName) {

        GenericResponse response = new GenericResponse();
        QueryDataResponse data = new QueryDataResponse();


        List<Map<String, Object>> records = executeQueryAndGetRecords(query, source, showRowLimit);
        data.setRecords(records);

        response.setData(data);
        response.setStatusCode(200);

        String csvFile = createCSVFile(data, query, dataDirectory, outputFileName);

        String table = generateTablePreview(csvFile, 15);

        response.setMessage(
                query + "\n" +
                        "Your response is saved to a CSV file named '" + csvFile + "'!" + "\n" +
                        "Table Output:" + "\n" +
                        table +
                        "..." + "\n" +
                        "Total rows: " + data.getRecords().size()
        );

        return response;
    }
}

