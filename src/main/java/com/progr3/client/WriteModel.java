package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

public class WriteModel {
    public WriteModel() {
    }

    public boolean sendMail(String to, String object, String content) throws IOException {
        String[] addresses = to.split(",");
        for (int i = 0; i < addresses.length; i++) {
            if (validateEmail(addresses[i])) {
                addresses[i] = addresses[i].trim();
            } else {
                return false;
            }
        }

        try {
            Socket clientSocket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(new Packet(PacketType.Send, new Email(ClientModel.account.getAddress(), Arrays.stream(addresses).toList(), object, content, null)));

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Packet pkt = (Packet) in.readObject();
            clientSocket.close();
            return (boolean) pkt.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
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
