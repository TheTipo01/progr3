package com.progr3.entities;

import java.io.Serializable;

/**
 * Represents an email account, with the associated email and password.
 */
public class Account implements Serializable {
    private final String address;
    private final String password;

    public Account(String address, String password) {
        this.address = address;
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }
}
