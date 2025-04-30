package model;

import mediator.ServerRequest;

public interface ClientMessageHandler {
    void handleMessage(ServerRequest message);
}
