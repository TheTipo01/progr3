package com.progr3.client;

public class Notify {
    private Integer sentMail;

    public Notify() {
        sentMail = 0;
    }

    public Integer getSentMail() {
        synchronized (sentMail) {
            return sentMail;
        }
    }

    public void setSentMail(int sentMail) {
        synchronized (this.sentMail) {
            this.sentMail = sentMail;
        }
    }

    public void incrementSentMail() {
        synchronized (sentMail) {
            sentMail++;
        }
    }
}
