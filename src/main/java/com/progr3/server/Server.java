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
import java.util.concurrent.Semaphore;

public class Server implements Runnable {
    private final Map<String, Manager> managers;
    private final List<ServerObserver> observers;
    private ServerSocket socket;
    private final ExecutorService pool;
    private final Semaphore semaphore;

    public void sendPacket(Socket socket, Packet<?> pkt) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(pkt);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(int port, List<ServerObserver> observers, int maxThreads) {
        this.managers = new HashMap<>();
        this.observers = observers;

        // Initializing Socket with exception handling
        try {
            this.socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reads the accounts from every directory and creates a manager for each one
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

        pool = Executors.newFixedThreadPool(maxThreads);
        // Semaphore to not exceed the maximum number of threads available in the pool
        semaphore = new Semaphore(maxThreads);
    }

    @Override
    public void run() {
        try {
            observers.forEach(ServerObserver::onStart);

            while (!Thread.interrupted()) {
                semaphore.acquire();
                Socket socket = this.socket.accept();
                observers.forEach(ServerObserver::onAccept);
                pool.submit(() -> handle(socket));
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

    public void handle(Socket socket) {
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Packet<?> packet = (Packet<?>) input.readObject();

            switch (packet.getType()) {
                case Login -> {
                    Account account = (Account) packet.getData();

                    sendPacket(socket, new Packet<>(PacketType.Error, !managers.containsKey(account.getAddress())));
                }
                case Inbox -> {
                    Inbox inbox = (Inbox) packet.getData();
                    Manager manager = managers.get(inbox.getAccount().getAddress());

                    List<Email> emails = null;
                    if (manager != null) {
                        emails = manager.getInbox();
                    }

                    sendPacket(socket, new Packet<>(PacketType.Inbox, new Inbox(inbox.getAccount(), emails)));
                }
                case Send -> {
                    Email email = (Email) packet.getData();
                    Manager manager = managers.get(email.getSender());

                    // Checks if the email has at least one receiver
                    if (email.getReceivers().size() == 0) {
                        sendPacket(socket, new Packet<>(PacketType.Error, true));
                        return;
                    }

                    // Writes the email to the receiver's inbox
                    ArrayList<String> notSent = new ArrayList<>();
                    for (String address : email.getReceivers()) {
                        if (managers.containsKey(address)) {
                            managers.get(address).writeEmail(email);
                        } else {
                            notSent.add(address);
                        }
                    }

                    // Sets the email as read for the writer
                    email.setRead();

                    if (notSent.size() == 0) {
                        manager.writeEmail(email);
                        sendPacket(socket, new Packet<>(PacketType.Error, false));
                    } else {
                        if (notSent.size() != email.getReceivers().size()) {
                            manager.writeEmail(email);
                            sendPacket(socket, new Packet<>(PacketType.ErrorPartialSend, notSent));
                            observers.forEach(o -> o.onError(null, new Exception("Email with id " + email.getId().toString() + " was not sent to " + notSent)));
                        } else {
                            sendPacket(socket, new Packet<>(PacketType.Error, true));
                            observers.forEach(o -> o.onError(null, new Exception("No receivers found for email with id " + email.getId().toString())));
                        }
                    }
                }
                case Delete -> {
                    Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();

                    Manager manager = managers.get(pair.getValue().getAddress());

                    sendPacket(socket, new Packet<>(PacketType.Error, manager.deleteEmail(pair.getKey())));
                }
                case Read -> {
                    Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();
                    Manager manager = managers.get(pair.getValue().getAddress());

                    Email email = pair.getKey();
                    email.setRead();

                    sendPacket(socket, new Packet<>(PacketType.Error, !manager.writeEmail(email)));
                }
            }

            input.close();

            observers.forEach(o -> o.onPacket(packet));
        } catch (Exception e) {
            observers.forEach(o -> o.onError(socket, e));
        } finally {
            try {
                socket.close();
                observers.forEach(ServerObserver::onClose);
                semaphore.release();
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
