package com.example.f_f.global.exception;

public class ConversationForbiddenException extends RuntimeException {
    public ConversationForbiddenException(Long id) {
        super("대화방 소유자가 아님: id=" + id);
    }
}