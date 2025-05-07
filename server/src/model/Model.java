package model;

import mediator.ServerRequest;
import utils.PropertyChangeSubject;


public interface Model extends PropertyChangeSubject
{
    /**
     * Henter profil manageren
     * @return profiles manager
     */
    Profiles getProfiles();

    /**
     * Henter message manageren
     * @return messages manager
     */
    Messages getMessages();

    /**
     * Henter chatrooms manageren
     * @return room manager
     */
    Rooms getRooms();

    /**
     * Sender en besked fra clienten videre til managers
     * @param message Requesten fra en client
     */
    void passClientMessage(ServerRequest message);
}
