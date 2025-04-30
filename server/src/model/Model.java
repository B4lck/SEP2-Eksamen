package model;

import mediator.ServerRequest;
import util.PropertyChangeSubject;


public interface Model extends PropertyChangeSubject
{
    Profiles getProfiles();
    void passClientMessage(ServerRequest message);
}
