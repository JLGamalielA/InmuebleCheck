package com.example.inmueblecheck;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String messageId;
    private String text;
    private String senderId;
    private String senderEmail;
    @ServerTimestamp
    private Date timestamp;

    public Message() {} // Constructor vacío requerido por Firestore

    public Message(String text, String senderId, String senderEmail) {
        this.text = text;
        this.senderId = senderId;
        this.senderEmail = senderEmail;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}