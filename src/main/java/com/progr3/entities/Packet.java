package com.progr3.entities;

import javafx.util.Pair;

import java.io.Serializable;
import java.util.List;

/**
 * Entity that defines a Packet and all its possible types, as described by
 * the PacketType class. Its generator checks the type of data that a Packet
 * is generated with, and throws an exception if it does not match the packet
 * type.
 */
public class Packet<T extends Serializable> implements Serializable {
    private final PacketType type;
    private final T data;

    public Packet(PacketType type, T data) {
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
            case Delete, Read -> {
                if (!(data instanceof Pair && ((Pair<?, ?>) data).getKey() instanceof Email && ((Pair<?, ?>) data).getValue() instanceof Account)) {
                    throw new IllegalArgumentException("(Delete packet) Data must be a Pair<Email, Account>");
                }
            }
            case Error -> {
                if (!(data instanceof Boolean)) {
                    throw new IllegalArgumentException("(Error packet) Data must be a Boolean");
                }
            }
            case ErrorPartialSend -> {
                if (!(data instanceof List && ((List<?>) data).get(0) instanceof String)) {
                    throw new IllegalArgumentException("(Partial Send packet) Data must be a List<String>");
                }
            }
            case ConnectionError -> {
                if (!(data instanceof Boolean)) {
                    throw new IllegalArgumentException("(Connection Error packet) Data must be a Boolean");
                }
            }
            default -> throw new IllegalArgumentException("(Packet) Invalid packet type");
        }

        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public T getData() {
        return data;
    }
}
