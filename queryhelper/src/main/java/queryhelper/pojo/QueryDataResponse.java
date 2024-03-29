package queryhelper.pojo;

import java.util.List;
import java.util.Map;

public class QueryDataResponse {
    private String schema;
    private Double responseTime;
    private String query;
    private List<Map<String, Object>> records;

    public QueryDataResponse() {
        this.schema = null;
        this.responseTime = null;
        this.query = null;
        this.records = null;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }
}
