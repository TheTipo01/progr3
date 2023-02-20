package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerListener extends Thread {
    private final ClientModel model;

    public ServerListener(ClientModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        try {
            // TODO: chiudere il socket :)
            Socket socket = new Socket(ClientMain.host, ClientMain.port);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(new Packet(PacketType.Notify, ClientModel.account));

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Packet pkt = (Packet) ois.readObject();
                if (pkt.getType() == PacketType.Send) {
                    model.addEmail((Email) pkt.getData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
