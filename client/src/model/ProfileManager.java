package model;

import java.util.ArrayList;

public class ProfileManager {
    private ArrayList<Profile> profiles;

    public long signUp(String username, String password) {
        return 0L;
    }

    public ArrayList<Profile> getAll() {
        return profiles;
    }

    public Profile getProfile(long id) {
        for (Profile profile : profiles) {
            if (profile.getUUID() == id) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Profilen kan ikke findes");
    }

    public Profile getCurrentUser() {
        return new Profile();
    }
}
