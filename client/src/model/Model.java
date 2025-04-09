package model;

import util.PropertyChangeSubject;

import java.util.ArrayList;

public interface Model extends PropertyChangeSubject {
    ProfileManager getProfiles();
}
