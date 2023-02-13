package com.progr3.entities;

import java.io.Serializable;

public class Packet implements Serializable {
    private final PacketType type;

    public Packet(PacketType type) {
        this.type = type;
    }

    public PacketType getType() {
        return type;
    }
}
