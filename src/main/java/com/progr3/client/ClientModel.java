package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import com.progr3.entities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientModel {
    public static Account account;
    private final ObservableList<Email> messages;
    private final NotifyController notifyController;

    public ClientModel(NotifyController notifyController) {
        messages = FXCollections.observableArrayList(new ArrayList<>());
        this.notifyController = notifyController;

        loadMessages();
    }

    private void loadMessages() {
        try {
            Socket socket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet(PacketType.Inbox, new Inbox(account, null)));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Packet packet = (Packet) ois.readObject();

            socket.close();

            Inbox inbox = (Inbox) packet.getData();

            if (packet.getType() == PacketType.Inbox) {
                messages.addAll(inbox.getEmails());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListenerMessages(ListChangeListener<Email> listener) {
        messages.addListener(listener);
    }

    public String getCurrentEmail() {
        return account.getAddress();
    }

    public void setAccount(Account account) {
        ClientModel.account = account;
    }

    public Email getMessage(int index) {
        synchronized (messages) {
            return messages.get(index);
        }
    }

    public int getMessagesSize() {
        synchronized (messages) {
            return messages.size();
        }
    }

    public boolean deleteEmail(Email email, Account account) {
        try {
            Socket socket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet(PacketType.Delete, new Pair<>(email, account)));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Packet packet = (Packet) ois.readObject();

            socket.close();

            synchronized (messages) {
                messages.remove(email);
            }

            return (boolean) packet.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void bindTableView(TableView<Email> tableView) {
        tableView.setItems(messages);
    }

    public void setMessages(List<Email> emails) {
        int difference = emails.size() - messages.size();

        synchronized (messages) {
            Platform.runLater(() -> messages.setAll(emails));
        }

        if (difference > 0 && (difference - notifyController.getSentMail()) > 0) {
            Platform.runLater(() -> {
                try {
                    PopupController.showPopup("Nuova mail", "Hai ricevuto una email!", ImageType.Success, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        notifyController.setSentMail(0);
    }
}