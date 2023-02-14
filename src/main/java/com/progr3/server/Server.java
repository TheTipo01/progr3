package com.progr3.server;

import com.progr3.entities.Account;
import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private Map<String, Manager> managers;
    private List<ServerObserver> observers;
    private ServerSocket socket;
    private ExecutorService pool;

    public Server(int port, List<ServerObserver> observers) {
        this.managers = new HashMap<>();
        this.observers = observers;

        // Initializing Socket with exception handling
        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        pool = Executors.newFixedThreadPool(6);
    }

    @Override
    public void run() {
        try {
            for (ServerObserver o : observers)
                o.onStart();

            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                pool.submit(() -> handle(clientSocket));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(Socket clientSocket) {
        try {
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            Packet packet = (Packet) input.readObject();

            switch (packet.getType()) {
                case Login -> {
                    Account account = (Account) packet.getData();
                    Manager manager = managers.get(account.getAddress());
                    // if (manager != null && manager.getAccount().verifyPassword(account.getPassword()))
                }
                case Inbox -> {
                    Account account = (Account) packet.getData();
                    Manager manager = managers.get(account.getAddress());

                    List<Email> emails = manager.getInbox();

                    ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                    output.writeObject(new Packet(PacketType.Inbox, emails));
                }
                case Send -> {
                    Email email = (Email) packet.getData();
                    Manager manager = managers.get(email.getSender());

                    manager.writeEmail(email);

                    for (String address : email.getReceivers())
                        managers.get(address).writeEmail(email);
                }
                case Delete -> {
                    Email email = (Email) packet.getData();
                    Manager manager = managers.get(email.getSender());

                    manager.deleteEmail(email);
                }
            }

            for (ServerObserver o : observers)
                o.onReceive(packet);


        } catch (Exception e) {
            for (ServerObserver o : observers)
                o.onError(clientSocket, e);
        }
    }
}
