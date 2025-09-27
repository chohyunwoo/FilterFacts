package com.example.f_f.global.exception;

public class ErrorResponse {

    private final String code;
    private final String message;

    private ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorResponse of(RsCode rsCode) {
        return new ErrorResponse(rsCode.getCode(), rsCode.getMessage());
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
