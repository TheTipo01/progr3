package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Inbox;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Thread used to poll the server for new messages
 */
public class ServerPoller extends Thread {
    private final ClientModel model;

    public ServerPoller(ClientModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(LoginMain.waitTime);
            } catch (InterruptedException e) {
                break;
            }

            try {
                Socket socket = new Socket(LoginMain.host, LoginMain.port);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(new Packet<>(PacketType.Inbox, new Inbox(ClientModel.account, null)));

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                Packet<Inbox> pkt = (Packet<Inbox>) ois.readObject();
                if (pkt.getType() == PacketType.Inbox) {
                    List<Email> emails = pkt.getData().getEmails();
                    if (emails.size() != model.getMessagesSize()) {
                        model.setMessages(emails);
                    }
                }

                ois.close();
                oos.close();
                socket.close();

                model.setServerOnline(true);
            } catch (Exception e) {
                model.setServerOnline(false);
            }
        }
    }
}
