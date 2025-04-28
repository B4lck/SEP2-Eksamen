package model;

import mediator.ClientMessage;

public interface BroadcastHandler {
    void receiveBroadcast(ClientMessage message);
}
