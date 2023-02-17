package com.progr3.entities;

import java.io.Serializable;

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
