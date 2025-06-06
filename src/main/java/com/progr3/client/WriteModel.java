package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

public class WriteModel {
    private Notify notify;

    public void setNotify(Notify notify) {
        this.notify = notify;
    }

    /**
     * Sends an email to the server
     *
     * @param email The email to send
     * @return A packet containing the result of the operation
     */
    public Packet<Boolean> sendMail(Email email) {
        if (email == null) {
            return new Packet<>(PacketType.Error, true);
        }

        try {
            Socket clientSocket = new Socket(LoginMain.host, LoginMain.port);

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(new Packet<>(PacketType.Send, email));

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Packet<Boolean> pkt = (Packet<Boolean>) in.readObject();

            clientSocket.close();

            notify.incrementSentMail();

            return pkt;
        } catch (Exception e) {
            return new Packet<>(PacketType.ConnectionError, true);
        }
    }

    /**
     * Support method to format the raw input from the UI
     *
     * @param to      A comma separated list of email addresses
     * @param object  The object of the email
     * @param content The content of the email
     * @return An email object if the input is valid, null otherwise
     */
    public Email formatEmail(String to, String object, String content) {
        String[] addresses = to.split(",");
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = addresses[i].trim();
            if (!validateEmail(addresses[i])) {
                return null;
            }
        }

        return new Email(ClientModel.account.getAddress(), Arrays.stream(addresses).toList(), object, content);
    }

    /**
     * Validates an email address
     *
     * @param email the email address to validate
     * @return true if the email address is valid, false otherwise
     */
    private static boolean validateEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }
}
