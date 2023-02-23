package com.progr3.client;

import com.progr3.entities.Account;
import com.progr3.entities.Packet;
import com.progr3.entities.PacketType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginModel {

    /**
     * Method use to verify the existence of an account.
     *
     * @param account The account to verify
     * @return True if the account exists, false otherwise
     * @throws Exception If an error connecting to the server occurs
     */
    public boolean verifyAccount(Account account) throws Exception {
        Socket clientSocket = new Socket(LoginMain.host, LoginMain.port);
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

        out.writeObject(new Packet<>(PacketType.Login, account));

        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        Packet<?> pkt = (Packet<?>) in.readObject();

        clientSocket.close();

        return (boolean) pkt.getData();
    }
}
