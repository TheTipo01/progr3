package com.progr3.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Email implements Serializable {
    private final UUID id;
    private final List<String> receivers;
    private final String text;
    private final Date timestamp;
    private final Email replyTo;
    private final String object;
    private final String sender;

    public Email(String sender, List<String> receivers, String object, String text, Email replyTo) {
        this.sender = sender;
        this.receivers = receivers;
        this.object = object;
        this.text = text;
        this.timestamp = new Date();
        this.id = UUID.randomUUID();
        this.replyTo = replyTo;
    }

    public UUID getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public String getObject() {
        return object;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Email getReplyTo() {
        return replyTo;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Email obj) {
            return this.id == obj.getId();
        }

        return false;
    }
}
