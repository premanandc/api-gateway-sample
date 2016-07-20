package com.premonition.web;

public class Message {
    private String value;

    @SuppressWarnings("unused")
    private Message() {
    }

    public static Message called(String value) {
        return new Message(value);
    }

    private Message(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
