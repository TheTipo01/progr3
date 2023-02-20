package com.progr3.server;

import com.progr3.entities.*;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final Map<String, Manager> managers;
    private final List<ServerObserver> observers;
    private ServerSocket socket;
    private final ExecutorService pool;

    public void sendPacket(Socket clientSocket, Packet pkt) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(pkt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

                    sendPacket(clientSocket, new Packet(PacketType.Error, !managers.containsKey(account.getAddress())));
                }
                case Inbox -> {
                    Inbox inbox = (Inbox) packet.getData();
                    Manager manager = managers.get(inbox.getAccount().getAddress());

                    List<Email> emails = null;
                    if (manager != null) {
                        emails = manager.getInbox();
                    }

                    sendPacket(clientSocket, new Packet(PacketType.Inbox, new Inbox(inbox.getAccount(), emails)));
                }
                case Send -> {
                    Email email = (Email) packet.getData();
                    Manager manager = managers.get(email.getSender());

                    if (email.getReceivers().size() == 0) {
                        sendPacket(clientSocket, new Packet(PacketType.Error, true));
                        return;
                    }

                    List<String> notSent = new ArrayList<>();
                    for (String address : email.getReceivers()) {
                        if (managers.containsKey(address)) {
                            managers.get(address).writeEmail(email);
                        } else {
                            notSent.add(address);
                        }
                    }

                    if (notSent.size() == 0) {
                        manager.writeEmail(email);
                        sendPacket(clientSocket, new Packet(PacketType.Error, false));
                    } else {
                        if (notSent.size() != email.getReceivers().size()) {
                            manager.writeEmail(email);
                            sendPacket(clientSocket, new Packet(PacketType.ErrorPartialSend, notSent));
                        } else {
                            sendPacket(clientSocket, new Packet(PacketType.Error, true));
                        }
                    }
                }
                case Delete -> {
                    Email email = (Email) packet.getData();
                    Manager manager = managers.get(email.getSender());

                    sendPacket(clientSocket, new Packet(PacketType.Error, !manager.deleteEmail(email)));
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
