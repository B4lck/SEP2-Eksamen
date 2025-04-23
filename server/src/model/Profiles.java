package model;

public interface Profiles extends ClientMessageHandler {
    Profile getProfile(long uuid);
    Profile getProfileByUsername(String username);
    void addProfile(Profile profile);
    void removeProfile(Profile profile);
}
