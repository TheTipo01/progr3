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

    /**
     * Method used to change server status
     *
     * @param online boolean value that defines the connection status
     */
    public void setServerOnline(boolean online) {
        serverOnline.set(online);
    }

    /**
     * This method is called for loading messages.
     *
     * @throws Exception If an error connecting to the server occurs
     */
    private void loadMessages() throws Exception {
        Socket socket = new Socket(LoginMain.host, LoginMain.port);

        // Asks the server for the inbox of our account
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(new Packet<>(PacketType.Inbox, new Inbox(account, null)));

        // And reads the response
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Packet<Inbox> packet = (Packet<Inbox>) ois.readObject();

        socket.close();

        Inbox inbox = packet.getData();

        if (packet.getType() == PacketType.Inbox) {
            messages.addAll(inbox.getEmails());
        }
    }

    /**
     * Method used to add a listener to the messages list
     *
     * @param listener a function that defines the listener's action
     */
    public void addListenerMessages(ListChangeListener<Email> listener) {
        messages.addListener(listener);
    }

    /**
     * Function that returns the current user's address
     *
     * @return String value of email address
     */
    public String getCurrentEmail() {
        return account.getAddress();
    }

    /**
     * Function that returns an email given its index
     *
     * @param index position of the email inside the messages list
     * @return Email object
     */
    public Email getMessage(int index) {
        synchronized (messages) {
            return messages.get(index);
        }
    }

    /**
     * Function that returns the quantity of emails inside the messages list
     *
     * @return int value of email number
     */
    public int getMessagesSize() {
        synchronized (messages) {
            return messages.size();
        }
    }

    /**
     * Method that calculates how many messages inside the list have not
     * been read for the current user,
     *
     * @return int value of unread messages count
     */
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

    /**
     * Method responsible for email deletion
     *
     * @param email   Email to be deleted
     * @param account Account where the email is
     * @return true if deletion is successful, false if server is not online
     */
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

    /**
     * Using a TableView from the Controller, the Model is able to bind the
     * messages to it, so the inbox can be viewed
     *
     * @param tableView TableView given by Controller
     */
    public void bindTableView(TableView<Email> tableView) {
        tableView.setItems(messages);
    }

    /**
     * Method used to add a listener to the server status value
     *
     * @param listener a function that defines the listener's action
     */
    public void addListenerOnline(ChangeListener<Boolean> listener) {
        serverOnline.addListener(listener);
    }

    /**
     * Method responsible for updating the messages list when getting a new
     * email.
     *
     * @param emails the list of emails given to the Model by the ServerPoller
     */
    public void setMessages(List<Email> emails) {
        // Calculating difference between new list and old list, effectively getting
        // how many new messages there are
        int difference = emails.size() - messages.size();

        // Sets the new list and sorts the list if there is more than 1 message
        Platform.runLater(() -> {
            synchronized (messages) {
                messages.setAll(emails);
                if (messages.size() > 1) {
                    messages.sort((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                }
            }
        });

        // Calculates the amount of new emails by checking how many of them
        // have been sent by the current account
        if (difference > 0 && (difference - notify.getSentMail()) > 0) {
            Platform.runLater(() -> {
                // If there actually are new emails sent by someone other than
                // the current user, shows a popup to alert the user
                PopupController.showPopup("Nuova mail", "Hai ricevuto una email!", ImageType.Success, null);
            });
        }

        // Resets the counter for how many emails the user has sent
        notify.setSentMail(0);
    }

    /**
     * Sets an email as read, changing the status also in the observable list
     *
     * @param email The email to be set as read
     */
    public void setEmailAsRead(Email email) {
        try {
            if (!email.isRead()) {
                Socket socket = new Socket(LoginMain.host, LoginMain.port);

                // Ask the server for set read an email
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Packet<>(PacketType.Read, new Pair<>(email, account)));

                // Read the response
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Packet<Boolean> packet = (Packet<Boolean>) ois.readObject();

                socket.close();

                // Set read an email int the observable list
                int index = messages.indexOf(email);
                email.setRead();

                if (packet.getData()) {
                    synchronized (messages) {
                        messages.set(index, email);
                    }
                }
            }
        } catch (Exception ignored) {
            // The only exception that can be thrown are those related to the socket. We can ignore them safely, as the
            // email will not be marked as read in the UI if something were to happen
        }
    }
}