package model;

import mediator.ServerRequest;
import util.PropertyChangeSubject;


public interface Model extends PropertyChangeSubject
{
    /**
     * Henter profil manageren
     * @return profiles
     */
    Profiles getProfiles();

    /**
     * Sender en besked fra clienten videre til modellen
     * @param message
     */
    void passClientMessage(ServerRequest message);
}
