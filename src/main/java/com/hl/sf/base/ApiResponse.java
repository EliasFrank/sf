package com.hl.sf.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hl2333
 */
@Data
public class ApiResponse {
    private int code;
    private String message;
    private Object data;
    private boolean more;

    public ApiResponse(){
        this.code = Status.SUCCESS.getCode();
        this.message = Status.SUCCESS.getStandardMessage();
    }
    public ApiResponse(int code, String message, Object data){
        setCode(code);
        setMessage(message);
        setData(data);
    }

    public static ApiResponse ofMessage(int code, String message){
        return new ApiResponse(code, message, null);
    }

    public static ApiResponse ofSuccess(Object data){
        return new ApiResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
    }

    public static ApiResponse ofStatus(Status status){
        return new ApiResponse(status.getCode(), status.getStandardMessage(), null);
    }
    @AllArgsConstructor
    @NoArgsConstructor
    public  enum Status{
        /**
         * 各种自定义的状态码
         */
        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "BadRequest"),
        NOT_FOUND(404, "Not Find"),
        INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
        NOT_VALID_PARAM(40005, "Not Valid Params"),
        NOT_SUPPORTED_OPERATION(40006, "Operation Not Supported"),
        NOT_LOGIN(5000, "Not Login");

        private int code;
        private String standardMessage;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getStandardMessage() {
            return standardMessage;
        }

        public void setStandardMessage(String standardMessage) {
            this.standardMessage = standardMessage;
        }
    }
}
