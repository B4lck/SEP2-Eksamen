package model;

import mediator.ServerMessage;

import java.util.ArrayList;

public class ProfilesArrayListManager implements Profiles {
    private ArrayList<Profile> profiles;

    public ProfilesArrayListManager() {
        profiles = new ArrayList<>();
        setDummyData();
    }

    private void setDummyData() {
        profiles.add(new ArrayListProfile("B4lcken", "HackerMan123"));
        profiles.add(new ArrayListProfile("Malthel", "PinkPonyClub"));
        profiles.add(new ArrayListProfile("Sharaf", "SwedenSucks321"));
    }

    @Override
    public Profile getProfile(long uuid) {
        for (Profile profile : profiles) {
            if (profile.getUUID() == uuid) {
                return profile;
            }
        }

        throw new IllegalArgumentException("User does not exist");
    }

    @Override
    public void addProfile(Profile profile) {
        profiles.add(profile);
    }

    @Override
    public void removeProfile(Profile profile) {
        profiles.remove(profile);
    }

    @Override
    public void handleMessage(ServerMessage message) {
        // TODO
    }
}
