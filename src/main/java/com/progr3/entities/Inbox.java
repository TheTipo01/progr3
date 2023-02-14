package com.progr3.entities;

import java.io.Serializable;
import java.util.List;

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
