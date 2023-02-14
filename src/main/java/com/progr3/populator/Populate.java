package com.progr3.populator;

import com.progr3.entities.Account;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Populate {
    public static void main(String[] args) throws Exception {
        Account account = new Account("manuel.raimo@edu.unito.it", "password");
        // Write the account to a file
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./posta/" + account.getAddress() + "/account"));
        oos.writeObject(account);
    }
}
