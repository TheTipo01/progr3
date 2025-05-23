package com.progr3.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entity that represents an email. Other than the basic properties, an email
 * is identified by its unique UUID, and has a flag that determines if it has been
 * read by the user.
 */
public class Email implements Serializable {
    private final UUID id;
    private final List<String> receivers;
    private final String text;
    private final Date timestamp;
    private final String object;
    private final String sender;
    private boolean read;

    public Email(String sender, List<String> receivers, String object, String text) {
        this.sender = sender;
        this.receivers = receivers;
        this.object = object;
        this.text = text;
        this.timestamp = new Date();
        this.id = UUID.randomUUID();
        this.read = false;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Email obj) {
            return this.id == obj.getId();
        }

        return false;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead() {
        read = true;
    }
}
