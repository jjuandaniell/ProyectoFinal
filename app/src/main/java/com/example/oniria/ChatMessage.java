package com.example.oniria;

public class ChatMessage {
    private String message;
    private boolean isUserMessage;
    private long timestamp;

    public ChatMessage(String message, boolean isUserMessage) {
        this.message = message;
        this.isUserMessage = isUserMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
