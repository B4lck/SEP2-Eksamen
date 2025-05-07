package model;

import mediator.ServerRequest;

public interface ServerRequestHandler {
    /**
     * Håndter en besked fra en client
     *
     * @param request beskeden fra clienten
     */
    void handleRequest(ServerRequest request);
}
