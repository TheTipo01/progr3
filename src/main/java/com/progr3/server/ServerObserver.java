package com.progr3.server;

import com.progr3.entities.Packet;

import java.net.Socket;

public interface ServerObserver {
    void onStart();

    void onShutdown();

    void onAccept();

    void onClose();

    void onPacket(Packet pkt);

    void onError(Socket clientSocket, Throwable exception);
}
