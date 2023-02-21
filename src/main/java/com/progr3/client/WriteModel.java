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
    private NotifyController notifyController;

    public void setNotifyController(NotifyController notifyController) {
        this.notifyController = notifyController;
    }

    public Packet sendMail(Email email) {
        if (email == null) {
            return new Packet(PacketType.Error, true);
        }

        try {
            Socket clientSocket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(new Packet(PacketType.Send, email));

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Packet pkt = (Packet) in.readObject();

            clientSocket.close();

            notifyController.incrementSentMail();

            return pkt;
        } catch (Exception e) {
            e.printStackTrace();
            return new Packet(PacketType.Error, true);
        }
    }

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
    public static boolean validateEmail(String email) {
        return Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(email).matches();
    }
}
