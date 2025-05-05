package model;

import mediator.ServerRequest;
import utils.PropertyChangeSubject;


public interface Model extends PropertyChangeSubject
{
    /**
     * Henter profil manageren
     * @return profiles
     */
    Profiles getProfiles();
    Messages getChat();
    Rooms getChatRooms();

    /**
     * Sender en besked fra clienten videre til modellen
     * @param message
     */
    void passClientMessage(ServerRequest message);
}
