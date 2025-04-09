package model;

import mediator.ServerMessage;

public interface ClientMessageHandler {
    void handleMessage(ServerMessage message);
}
