package model;

import utils.PropertyChangeSubject;

public interface Model extends PropertyChangeSubject, ServerRequestForwarder {
    /**
     * Henter profil manageren
     *
     * @return profiles manager
     */
    Profiles getProfiles();

    /**
     * Henter message manageren
     *
     * @return messages manager
     */
    Messages getMessages();

    /**
     * Henter chatrooms manageren
     *
     * @return room manager
     */
    Rooms getRooms();
}
