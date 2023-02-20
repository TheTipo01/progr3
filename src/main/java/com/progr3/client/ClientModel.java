package com.progr3.client;

import com.progr3.entities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ClientModel {
    public static Account account;
    final ObservableList<Email> messages;

    public ClientModel(TitledPane titledPane, TableView<Email> tableView, TextArea textArea) {
        messages = FXCollections.observableArrayList(new ArrayList<>());

        viewMessagesStartup(tableView);
        displayContent(tableView, textArea);

        titledPane.textProperty().setValue("Ciao, " + getCurrentEmail() + " (" + getMessagesSize() + " messaggi)");

        messages.addListener((ListChangeListener) c -> {
            Platform.runLater(() -> {
                titledPane.textProperty().setValue("Ciao, " + getCurrentEmail() + " (" + getMessagesSize() + " messaggi)");
                try {
                    PopupController.showPopup("Nuova mail", "Hai ricevuto una email!", ImageType.Success, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private String getCurrentEmail() {
        return account.getAddress();
    }

    private void displayContent(TableView<Email> tableView, TextArea textArea) {
        // Stolen from stack overflow
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        synchronized (messages) {
            selectedCells.addListener((ListChangeListener) c -> {
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);

                textArea.setText(messages.get(tablePosition.getRow()).getText());
            });
        }
    }

    private void viewMessagesStartup(TableView<Email> tv) {
        synchronized (messages) {
            loadMessages();

            if (!messages.isEmpty()) {
                TableColumn<Email, String> objectCol = new TableColumn<>("Oggetto");
                objectCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getObject()));
                TableColumn<Email, String> senderCol = new TableColumn<>("Mittente");
                senderCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSender()));
                TableColumn<Email, String> dateCol = new TableColumn<>("Data");

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                dateCol.setCellValueFactory(p -> new SimpleStringProperty(sdf.format(p.getValue().getTimestamp())));

                tv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                tv.getColumns().addAll(objectCol, senderCol, dateCol);
                tv.setItems(messages);
            }
        }
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

    public void setAccount(Account account) {
        this.account = account;
    }

    public void addEmail(Email email) {
        synchronized (messages) {
            messages.add(email);
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

}