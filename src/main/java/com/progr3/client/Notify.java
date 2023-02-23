package com.progr3.client;

/**
 * A class used to keep track of the number of emails sent by the client, to know when to show a popup about new messages.
 */
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
