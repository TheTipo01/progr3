package com.progr3.server.logger;

import com.progr3.entities.Account;
import com.progr3.entities.Email;
import com.progr3.entities.Inbox;
import com.progr3.entities.Packet;
import com.progr3.server.ServerObserver;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.net.Socket;
import java.util.Date;

public class ServerLogger implements ServerObserver {
    @FXML
    private TextArea textArea;

    private final Level level;

    public ServerLogger() {
        level = Level.INFO;
    }

    private void log(String message, Level level) {
        if (level.compareTo(this.level) >= 0) {
            String logMessage = String.format("[%s] %s: %s\n", new Date(), level, message);
            textArea.appendText(logMessage);
        }
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
    public void onPacket(Packet pkt) {
        switch (pkt.getType()) {
            case Login -> {
                Account account = (Account) pkt.getData();
                String message = String.format("User %s logged in", account.getAddress());
                log(message, Level.INFO);
            }
            case Send -> {
                Email email = (Email) pkt.getData();
                String message = String.format("%s sent an email to %s", email.getSender(), email.getReceivers().toString());
                log(message, Level.INFO);
            }
            case Inbox -> {
                Inbox inbox = (Inbox) pkt.getData();
                String message = String.format("User %s loaded their inbox", inbox.getAccount().getAddress());
                log(message, Level.DEBUG);
            }
            case Delete -> {
                Email email = (Email) pkt.getData();
                String message = String.format("Email with id %s has been deleted", email.getId().toString());
                log(message, Level.INFO);
            }
            case Read -> {
                Email email = (Email) pkt.getData();
                String message = String.format("Email with id %s has been read", email.getId().toString());
                log(message, Level.INFO);
            }
            default -> log("Unhandled packet received", Level.WARNING);
        }
    }

    @Override
    public void onError(Socket clientSocket, Throwable exception) {
        log(exception.getMessage(), Level.ERROR);
    }
}
