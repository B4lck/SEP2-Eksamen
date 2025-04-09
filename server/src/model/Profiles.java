package model;

public interface Profiles extends ClientMessageHandler {
    Profile getProfile(long uuid);
    void addProfile(Profile profile);
    void removeProfile(Profile profile);
}
