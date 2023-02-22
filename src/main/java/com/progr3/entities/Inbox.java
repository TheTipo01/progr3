package com.progr3.entities;

import java.io.Serializable;
import java.util.List;

/**
 * Entity that represents the inbox of an account. It is used to get all the
 * emails of a specific account, hence why it needs to be generated with a List.
 */
public class Inbox implements Serializable {
    private final Account account;
    private final List<Email> emails;

    public Inbox(Account account, List<Email> emails) {
        this.account = account;
        this.emails = emails;
    }

    public Account getAccount() {
        return account;
    }

    public List<Email> getEmails() {
        return emails;
    }
}
