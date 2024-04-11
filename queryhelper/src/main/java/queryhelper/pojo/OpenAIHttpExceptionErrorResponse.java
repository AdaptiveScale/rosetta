package queryhelper.pojo;

public class OpenAIHttpExceptionErrorResponse {
    private ErrorDetails error;

    public OpenAIHttpExceptionErrorResponse() {
        this.error = null;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    public static class ErrorDetails {
        private String message;
        private String type;
        private String param;
        private String code;

        public ErrorDetails() {
            this.message = null;
            this.type = null;
            this.param = null;
            this.code = null;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
