package model;

import java.util.ArrayList;
import java.util.Random;

public class ProfileManager {
    private ArrayList<Profile> profiles;
    private long currentUserId;

    // TEMP
    private Random random;

    public ProfileManager() {
        profiles = new ArrayList<>();
        random = new Random();

        currentUserId = -1;
    }

    public long signUp(String username, String password) {
        profiles.add(new Profile(username, password, random.nextLong()));

        long id = profiles.getLast().getUUID();
        this.currentUserId = id;
        return id;
    }

    public ArrayList<Profile> getAll() {
        return profiles;
    }

    public void logout() {
        this.currentUserId = -1;
    }

    public long login(String username, String password) {
        for (Profile profile : profiles) {
            if (username.equalsIgnoreCase(profile.getUsername()) && password.equals(profile.getPassword())) {
                this.currentUserId = profile.getUUID();
                return currentUserId;
            }
        }
        throw new IllegalArgumentException("Brugernavn eller password er ikke gyldig");
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
        return getProfile(currentUserId);
    }
}
