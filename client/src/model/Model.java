package model;

import util.PropertyChangeSubject;

import java.util.ArrayList;

public interface Model extends PropertyChangeSubject {
    ArrayList<Profile> getProfiles();
}
