package queryhelper.pojo;

public class GenericResponse {
    private Object data;
    private String message;
    private Integer statusCode;

    public GenericResponse() {
        this.data = null;
        this.message = null;
        this.statusCode = null;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

}
