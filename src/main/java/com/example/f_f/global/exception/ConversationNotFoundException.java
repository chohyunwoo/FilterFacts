package com.example.f_f.global.exception;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(Long id) {
        super("존재하지 않는 대화방: id=" + id);
    }
}
