package com.progr3.server.Logger;

import com.progr3.entities.Account;
import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import com.progr3.server.ServerObserver;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.net.Socket;
import java.util.Date;

public class ServerLogger implements ServerObserver {
    @FXML
    public TextArea textArea;

    private void log(String message, Level level) {
        Date date = new Date();

        String logMessage = String.format("[%s] %s: %s\n", date, level.toString(), message);
        textArea.appendText(logMessage);
    }

    @Override
    public void onStart() {
        log("Server started", Level.INFO);
    }

    @Override
    public void onShutdown() {
        log("Server shut down", Level.INFO);
    }

    @Override
    public void onReceive(Packet pkt) {
        switch (pkt.getType()) {
            case Login:
                Account account = (Account) pkt.getData();
                String message = String.format("User %s logged in", account.getAddress());
                log(message, Level.INFO);
                break;

            case Send:
                Email email = (Email) pkt.getData();
                message = String.format("%s sent an email to %s", email.getSender(), email.getReceivers().toString());
                log(message, Level.INFO);
                break;

            case Inbox:
                break;

            case Delete:
                email = (Email) pkt.getData();
                message = String.format("Email with id %s has been deleted", email.getId().toString());
                log(message, Level.INFO);
                break;

            case Read:
                email = (Email) pkt.getData();
                message = String.format("Email with id %s has been read", email.getId().toString());
                log(message, Level.INFO);
                break;

            default:
                log("Unhandled packet received", Level.WARNING);
                break;
        }
    }

    @Override
    public void onError(Socket clientSocket, Throwable exception) {
        log(exception.getMessage(), Level.ERROR);
    }
}
