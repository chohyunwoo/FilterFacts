package com.example.f_f.global.exception;

public class CustomException extends RuntimeException {
    private final RsCode rsCode;

    public CustomException(RsCode rsCode) {
        super(rsCode.getMessage());
        this.rsCode = rsCode;
    }

    public RsCode getRsCode() {
        return rsCode;
    }
}
