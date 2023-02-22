package com.progr3.server;

import com.progr3.entities.*;
import javafx.util.Pair;

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
            oos.close();
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
            observers.forEach(ServerObserver::onStart);

            while (!Thread.interrupted()) {
                Socket clientSocket = socket.accept();
                pool.submit(() -> handle(clientSocket));
            }

        } catch (Exception ignored) {
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            pool.shutdownNow();
            observers.forEach(ServerObserver::onShutdown);
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

                    email.setRead();
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
                    Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();

                    Manager manager = managers.get(pair.getValue().getAddress());

                    sendPacket(clientSocket, new Packet(PacketType.Error, !manager.deleteEmail(pair.getKey())));
                }
                case Read -> {
                    Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();
                    Manager manager = managers.get(pair.getValue().getAddress());

                    Email email = pair.getKey();
                    email.setRead();

                    sendPacket(clientSocket, new Packet(PacketType.Error, !manager.writeEmail(email)));
                }
            }

            input.close();

            observers.forEach(o -> o.onPacket(packet));
        } catch (Exception e) {
            observers.forEach(o -> o.onError(clientSocket, e));
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                observers.forEach(ServerObserver::onShutdown);
            } catch (IOException e) {
                observers.forEach(o -> o.onError(null, e));
            }
        }
    }
}
