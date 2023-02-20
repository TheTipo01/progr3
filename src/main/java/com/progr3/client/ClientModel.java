package com.progr3.client;

import com.progr3.entities.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientModel {
    public static Account account;
    ObservableList<Email> messages;


    public ClientModel(TitledPane titledPane) {
        titledPane.textProperty().setValue("Hello, " + getCurrentEmail());
    }

    private String getCurrentEmail() {
        return account.getAddress();
    }

    public void displayContent(TableView tableView, TextArea textArea) {
        // Stolen from stack overflow
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener((ListChangeListener) c -> {
            TablePosition tablePosition = (TablePosition) selectedCells.get(0);

            textArea.setText(messages.get(tablePosition.getRow()).getText());
        });
    }

    public void viewMessagesStartup(TableView<Email> tv) {
        messages = FXCollections.observableArrayList(new ArrayList<>());
        loadMessages();

        if (!messages.isEmpty()) {
            TableColumn<Email, String> objectCol = new TableColumn<>("Object");
            objectCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getObject()));
            TableColumn<Email, String> senderCol = new TableColumn<>("Sender");
            senderCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSender()));

            tv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tv.getColumns().addAll(objectCol, senderCol);
            tv.setItems(messages);
        }
    }

    public void loadMessages() {
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

    public boolean deleteEmail(Email email, Account account) {
        try {
            Socket socket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet(PacketType.Delete, new Pair<>(email, account)));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Packet packet = (Packet) ois.readObject();

            socket.close();

            return (boolean) packet.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}