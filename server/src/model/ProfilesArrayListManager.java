package model;

import mediator.ClientMessage;
import mediator.ServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfilesArrayListManager implements Profiles {
    private ArrayList<Profile> profiles;

    public ProfilesArrayListManager() {
        profiles = new ArrayList<>();
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
        ArrayList<Map<String, Object>> profiles;

        try {
            switch (message.getType()) {
                // Sign up
                case "SIGN_UP":
                    System.out.println("sign up");
                    // Check if username is taken
                    try {
                        getProfileByUsername((String) message.getData().get("username"));
                        System.out.println("sendt fejl til bruger");
                        message.respond(new ClientMessage("Username is already taken"));
                        return;
                    } catch (IllegalArgumentException e) {
                        // Bruger findes ikke, så vi kan oprette den
                    }
                    System.out.println("a");
                    // Create user
                    user = new ArrayListProfile((String) message.getData().get("username"), (String) message.getData().get("password"));
                    addProfile(user);
                    System.out.println("b");
                    // Log user in
                    message.setUser(user.getUUID());
                    // Respond with uuid
                    message.respond(new ClientMessage("SIGN_UP", Map.of("uuid", Long.toString(user.getUUID()))));
                    System.out.println("sendt svar til bruger");
                    break;
                // Log in
                case "LOG_IN":
                    // Check if user exists
                    try {
                        user = getProfileByUsername((String) message.getData().get("username"));
                    } catch (IllegalArgumentException e) {
                        message.respond(new ClientMessage("Wrong username or password"));
                        return;
                    }
                    // Check password
                    if (user.checkPassword((String) message.getData().get("password"))) {
                        message.setUser(user.getUUID());
                        message.respond(new ClientMessage("LOG_IN", Map.of("uuid", Long.toString(user.getUUID()))));
                    } else {
                        message.respond(new ClientMessage("Wrong username or password"));
                    }
                    break;
                // Get profile
                case "GET_CURRENT_PROFILE":
                    message.respond(new ClientMessage("GET_PROFILE", Map.of("profile", getProfile(message.getUser()).getData())));
                    break;
                // Get profile
                case "GET_PROFILE":
                    message.respond(new ClientMessage("GET_PROFILE", Map.of("profile", getProfile(Long.parseLong((String) message.getData().get("uuid"))).getData())));
                    break;
                case "GET_PROFILES":
                    profiles = new ArrayList<>();
                    for (String id : (ArrayList<String>) message.getData().get("profiles")) {
                        profiles.add(getProfile(Long.parseLong(id)).getData());
                    }
                    message.respond(new ClientMessage("GET_PROFILES", Map.of("profiles", profiles)));
                    break;
                case "SEARCH_PROFILES":
                    profiles = new ArrayList<>();
                    for (Profile profile : searchProfiles((String) message.getData().get("query"))) {
                        profiles.add(profile.getData());
                    }
                    message.respond(new ClientMessage("GET_PROFILES", Map.of("profiles", profiles)));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }
}