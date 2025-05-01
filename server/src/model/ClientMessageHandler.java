package model;

import mediator.ServerRequest;

public interface ClientMessageHandler {
    /**
     * Håndter en besked fra en client
     * @param message beskeden fra clienten
     */
    void handleMessage(ServerRequest message);
}
