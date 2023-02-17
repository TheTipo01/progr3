package com.progr3.client;

import com.progr3.entities.Account;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginModel {

    public boolean verifyAccount(Account account) {
        try {
            Socket clientSocket = new Socket(ClientMain.host, ClientMain.port);
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            out.writeObject(new Packet(PacketType.Login, account));

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            Packet pkt = (Packet) in.readObject();

            clientSocket.close();

            return (boolean) pkt.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
