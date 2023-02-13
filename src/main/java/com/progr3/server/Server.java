package com.progr3.server;

import com.progr3.entities.Account;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {
    private final int port;
    private Map<String, Manager> managers;

    public Server(int port) {
        this.port = port;
        this.managers = new HashMap<>();

        // Reads the accounts from every directory
        // and creates a manager for each one
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("./posta"));
            for (Path dir : stream) {
                try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(dir.toString() + "/account"))) {
                    Account account = (Account) input.readObject();
                    managers.put(account.getAddress(), new Manager(account));
                } catch (ClassNotFoundException | ClassCastException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}
