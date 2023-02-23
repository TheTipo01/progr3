package com.progr3.server.logger;

import com.progr3.entities.Account;
import com.progr3.entities.Email;
import com.progr3.entities.Inbox;
import com.progr3.entities.Packet;
import com.progr3.server.ServerObserver;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.util.Pair;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLogger implements ServerObserver {
    @FXML
    private TextArea textArea;

    private final Level level;
    private final SimpleDateFormat format;

    public ServerLogger() {
        level = Level.INFO;
        format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    private void log(String message, Level level) {
        if (level.compareTo(this.level) >= 0) {
            String logMessage = String.format("[%s] %s: %s\n", format.format(new Date()), level, message);
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
    public void onPacket(Packet packet) {
        switch (packet.getType()) {
            case Login -> {
                Account account = (Account) packet.getData();
                String message = String.format("User %s logged in", account.getAddress());
                log(message, Level.INFO);
            }
            case Send -> {
                Email email = (Email) packet.getData();
                String message = String.format("%s sent an email with id %s to %s", email.getSender(), email.getId().toString(), email.getReceivers().toString());
                log(message, Level.INFO);
            }
            case Inbox -> {
                Inbox inbox = (Inbox) packet.getData();
                String message = String.format("User %s loaded their inbox", inbox.getAccount().getAddress());
                log(message, Level.DEBUG);
            }
            case Delete -> {
                Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();
                String message = String.format("%s deleted an email with id %s", pair.getValue().getAddress(), pair.getKey().getId().toString());
                log(message, Level.INFO);
            }
            case Read -> {
                Pair<Email, Account> pair = (Pair<Email, Account>) packet.getData();
                String message = String.format("%s read an email with id %s", pair.getValue().getAddress(), pair.getKey().getId().toString());
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
