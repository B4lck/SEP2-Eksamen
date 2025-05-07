package model;

import mediator.ServerRequest;

public interface ServerRequestPasser {
    void addHandler(ServerRequestHandler handler);

    void removeHandler(ServerRequestHandler handler);

    /**
     * Sender en besked fra clienten videre til managers
     *
     * @param request Requesten fra en client
     */
    void passServerRequest(ServerRequest request);
}
