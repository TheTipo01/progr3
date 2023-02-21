package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Inbox;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ServerListener extends Thread {
    private final ClientModel model;

    public ServerListener(ClientModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket socket = new Socket(ClientMain.host, ClientMain.port);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Packet(PacketType.Inbox, new Inbox(ClientModel.account, null)));

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                Packet pkt = (Packet) ois.readObject();
                if (pkt.getType() == PacketType.Inbox) {
                    List<Email> emails = ((Inbox) pkt.getData()).getEmails();
                    if (emails.size() != model.getMessagesSize()) {
                        model.setMessages(emails);
                    }
                }

                ois.close();
                oos.close();
                socket.close();

                Thread.sleep(ClientMain.waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
