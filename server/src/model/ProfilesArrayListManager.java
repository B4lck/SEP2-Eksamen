package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfilesArrayListManager implements Profiles {
    private ArrayList<Profile> profiles;
    private Model model;

    public ProfilesArrayListManager(Model model) {
        profiles = new ArrayList<>();
        this.model = model;
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
    public Profile getProfileByUsername(String username) {
        for (Profile profile : profiles) {
            if (username.equals(profile.getUsername())) {
                return profile;
            }
        }

        throw new IllegalArgumentException("User does not exist");
    }

    @Override
    public Profile createProfile(String username, String password) {
        var profile = new ArrayListProfile(username, password);
        addProfile(profile);
        return profile;
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
    public List<Profile> searchProfiles(String query) {
        return profiles.stream().filter(p -> p.getUsername().toLowerCase().contains(query.toLowerCase())).toList();
    }

    @Override
    public void handleRequest(ServerRequest request) {
        Profile user;
        ArrayList<DataMap> profiles;

        var data = request.getData();

        try {
            switch (request.getType()) {
                // Sign up
                case "SIGN_UP":
                    // Check if username is taken
                    try {
                        getProfileByUsername(data.getString("username"));
                        request.respond(new ClientMessage("Username is already taken"));
                        return;
                    } catch (IllegalArgumentException e) {
                        // Bruger findes ikke, så vi kan oprette den
                    }
                    // Create user
                    user = createProfile(data.getString("username"), data.getString("password"));
                    // Log user in
                    request.setUser(user.getUUID());
                    // Respond with uuid
                    request.respond(new DataMap().with("uuid", user.getUUID()));
                    break;
                // Log in
                case "LOG_IN":
                    // Check if user exists
                    try {
                        user = getProfileByUsername(data.getString("username"));
                    } catch (IllegalArgumentException e) {
                        request.respond(new ClientMessage("Wrong username or password"));
                        return;
                    }
                    // Check password
                    if (user.checkPassword(data.getString("password"))) {
                        request.setUser(user.getUUID());
                        request.respond(new DataMap().with("uuid", user.getUUID()));
                    } else {
                        request.respond(new ClientMessage("Wrong username or password"));
                    }
                    break;
                // Get profile
                case "GET_CURRENT_PROFILE":
                    request.respond(new DataMap().with("profile", getProfile(request.getUser()).getData()));
                    break;
                // Get profile
                case "GET_PROFILE":
                    request.respond(new DataMap().with("profile", getProfile(data.getLong("uuid")).getData()));
                    break;
                case "GET_PROFILES":
                    profiles = new ArrayList<>();
                    for (long id : data.getLongsArray("profiles")) {
                        profiles.add(getProfile(id).getData());
                    }
                    request.respond(new DataMap().with("profiles", profiles));
                    break;
                case "SEARCH_PROFILES":
                    profiles = new ArrayList<>();
                    for (Profile profile : searchProfiles(data.getString("query"))) {
                        profiles.add(profile.getData());
                    }
                    request.respond(new DataMap().with("profiles", profiles));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respond(new ClientMessage(e.getMessage()));
        }
    }
}