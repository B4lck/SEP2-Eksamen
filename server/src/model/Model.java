package model;

import mediator.ServerMessage;
import util.PropertyChangeSubject;


public interface Model extends PropertyChangeSubject
{
    Profiles getProfiles();
    void passClientMessage(ServerMessage message);
}
