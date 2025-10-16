package com.example.f_f.global.exception;

public class AiUpstreamException extends RuntimeException {
    private final int status;
    private final String responseBody;

    public AiUpstreamException(int status, String responseBody) {
        super("AI 연동 실패: status=" + status);
        this.status = status;
        this.responseBody = responseBody;
    }

    public int getStatus() { return status; }
    public String getResponseBody() { return responseBody; }
}