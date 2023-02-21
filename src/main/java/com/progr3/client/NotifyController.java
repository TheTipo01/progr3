package com.progr3.client;

public class NotifyController {
    private Integer sentMail;

    public NotifyController() {
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

    public void incrementSetMail() {
        synchronized (sentMail) {
            sentMail++;
        }
    }
}
