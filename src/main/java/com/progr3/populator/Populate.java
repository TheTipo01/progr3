package com.progr3.populator;

import com.progr3.entities.Account;
import com.progr3.entities.Email;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Populate {
    public static void main(String[] args) throws Exception {
        Account account = new Account("manuel.raimo@edu.unito.it", "password");
        // Write the account to a file
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./posta/" + account.getAddress() + "/account"));
        oos.writeObject(account);

        List<String> receivers = new ArrayList<>();
        receivers.add(account.getAddress());

        Email email = new Email("qqSD7xKfSmjk0wK67ep@yhtwtombr.marchildren.in.net", receivers, "HAI IL LIDL 500€", "Sei stato selezionato questa settimana come vincitore!", null);
        oos = new ObjectOutputStream(new FileOutputStream("./posta/" + email.getId().toString()));
        oos.writeObject(email);
    }
}
