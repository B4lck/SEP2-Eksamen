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
    public void handleMessage(ServerRequest message) {
        Profile user;
        ArrayList<DataMap> profiles;

        var request = message.getData();

        try {
            switch (message.getType()) {
                // Sign up
                case "SIGN_UP":
                    System.out.println("sign up");
                    // Check if username is taken
                    try {
                        getProfileByUsername(request.getString("username"));
                        System.out.println("sendt fejl til bruger");
                        message.respond(new ClientMessage("Username is already taken"));
                        return;
                    } catch (IllegalArgumentException e) {
                        // Bruger findes ikke, så vi kan oprette den
                    }
                    // Create user
                    user = createProfile(request.getString("username"), request.getString("password"));
                    // Log user in
                    message.setUser(user.getUUID());
                    // Respond with uuid
                    message.respond(new ClientMessage("SIGN_UP", new DataMap()
                            .with("uuid", user.getUUID())));
                    System.out.println("sendt svar til bruger");
                    break;
                // Log in
                case "LOG_IN":
                    // Check if user exists
                    try {
                        user = getProfileByUsername(request.getString("username"));
                    } catch (IllegalArgumentException e) {
                        message.respond(new ClientMessage("Wrong username or password"));
                        return;
                    }
                    // Check password
                    if (user.checkPassword(request.getString("password"))) {
                        message.setUser(user.getUUID());
                        message.respond(new ClientMessage("LOG_IN", new DataMap()
                                .with("uuid", Long.toString(user.getUUID()))));
                    } else {
                        message.respond(new ClientMessage("Wrong username or password"));
                    }
                    break;
                // Get profile
                case "GET_CURRENT_PROFILE":
                    message.respond(new ClientMessage("GET_PROFILE", new DataMap()
                            .with("profile", getProfile(message.getUser()).getData())));
                    break;
                // Get profile
                case "GET_PROFILE":
                    message.respond(new ClientMessage("GET_PROFILE", new DataMap()
                            .with("profile", getProfile(request.getLong("uuid")).getData())));
                    break;
                case "GET_PROFILES":
                    profiles = new ArrayList<>();
                    for (long id : request.getLongsArray("profiles")) {
                        profiles.add(getProfile(id).getData());
                    }
                    message.respond(new ClientMessage("GET_PROFILES", new DataMap().with("profiles", profiles)));
                    break;
                case "SEARCH_PROFILES":
                    profiles = new ArrayList<>();
                    for (Profile profile : searchProfiles(request.getString("query"))) {
                        profiles.add(profile.getData());
                    }
                    message.respond(new ClientMessage("GET_PROFILES", new DataMap().with("profiles", profiles)));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }
}