package com.progr3.entities;

import java.io.Serializable;

public class Packet implements Serializable {
    private final PacketType type;
    private final Object data;

    public Packet(PacketType type, Object data) {
        this.type = type;
        switch (type) {
            case Login -> {
                if (!(data instanceof Account)) {
                    throw new IllegalArgumentException("(Login packet) Data must be an Account");
                }
            }
            case Inbox -> {
                if (!(data instanceof Inbox)) {
                    throw new IllegalArgumentException("(Inbox packet) Data must be an Inbox");
                }
            }
            case Send -> {
                if (!(data instanceof Email)) {
                    throw new IllegalArgumentException("(Send packet) Data must be an Email");
                }
            }
            case Delete -> {
                if (!(data instanceof Email)) {
                    throw new IllegalArgumentException("(Delete packet) Data must be an Email");
                }
            }
            default -> throw new IllegalArgumentException("(Packet) Invalid packet type");
        }

        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
