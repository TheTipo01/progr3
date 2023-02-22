package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import com.progr3.entities.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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
    private final Notify notify;
    private final BooleanProperty serverOnline;

    public ClientModel(Notify notify) throws Exception {
        messages = FXCollections.observableArrayList(new ArrayList<>());
        this.notify = notify;
        serverOnline = new SimpleBooleanProperty(true);

        loadMessages();
    }

    public void setServerOnline(boolean online) {
        serverOnline.set(online);
    }

    private void loadMessages() throws Exception {
        Socket socket = new Socket(LoginMain.host, LoginMain.port);

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(new Packet<>(PacketType.Inbox, new Inbox(account, null)));

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Packet<Inbox> packet = (Packet<Inbox>) ois.readObject();

        socket.close();

        Inbox inbox = packet.getData();

        if (packet.getType() == PacketType.Inbox) {
            messages.addAll(inbox.getEmails());
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

    public int getNotReadMessages() {
        int count = 0;
        synchronized (messages) {
            for (Email email : messages) {
                if (!email.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean deleteEmail(Email email, Account account) {
        try {
            Socket socket = new Socket(LoginMain.host, LoginMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet<>(PacketType.Delete, new Pair<>(email, account)));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Packet<Boolean> packet = (Packet<Boolean>) ois.readObject();

            socket.close();

            synchronized (messages) {
                messages.remove(email);
            }

            return packet.getData();
        } catch (Exception e) {
            return false;
        }
    }

    public void bindTableView(TableView<Email> tableView) {
        tableView.setItems(messages);
    }

    public void addListenerOnline(ChangeListener<Boolean> listener) {
        serverOnline.addListener(listener);
    }

    public void setMessages(List<Email> emails) {
        int difference = emails.size() - messages.size();

        synchronized (messages) {
            Platform.runLater(() -> messages.setAll(emails));
            if (messages.size() > 1) {
                messages.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
            }
        }

        if (difference > 0 && (difference - notify.getSentMail()) > 0) {
            Platform.runLater(() -> {
                try {
                    PopupController.showPopup("Nuova mail", "Hai ricevuto una email!", ImageType.Success, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        notify.setSentMail(0);
    }

    public void setEmailAsRead(Email email) throws Exception {
        if (!email.isRead()) {
            Socket socket = new Socket(LoginMain.host, LoginMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet<>(PacketType.Read, new Pair<>(email, account)));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Packet<Boolean> packet = (Packet<Boolean>) ois.readObject();

            socket.close();

            int index = messages.indexOf(email);
            email.setRead();

            if (packet.getData()) {
                synchronized (messages) {
                    messages.set(index, email);
                }
            }
        }
    }
}