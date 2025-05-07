package model;

import mediator.ServerRequest;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfilesArrayListManager implements Profiles {
    private ArrayList<Profile> profiles;
    private Model model;

    public ProfilesArrayListManager(Model model) {
        profiles = new ArrayList<>();
        this.model = model;
        model.addHandler(this);
    }

    @Override
    public Profile getProfile(long uuid) { // TODO: Optional?
        for (Profile profile : profiles) {
            if (profile.getUUID() == uuid) {
                return profile;
            }
        }

        throw new IllegalStateException("User does not exist");
    }

    @Override
    public boolean doesProfileExist(long uuid) {
        for (Profile profile : profiles) {
            if (profile.getUUID() == uuid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<Profile> getProfileByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");

        for (Profile profile : profiles) {
            if (username.equals(profile.getUsername())) {
                return Optional.of(profile);
            }
        }

        return Optional.empty();
    }

    @Override
    public Profile createProfile(String username, String password) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        var profile = new ArrayListProfile(username, password);
        addProfile(profile);
        return profile;
    }

    @Override
    public void addProfile(Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Profile cannot be null");
        if (doesProfileExist(profile.getUUID())) throw new IllegalStateException("User already exists");
        if (getProfileByUsername(profile.getUsername()).isPresent())
            throw new IllegalStateException("Username already taken");
        profiles.add(profile);
    }

    @Override
    public void removeProfile(Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Profile cannot be null");
        profiles.remove(profile);
    }

    @Override
    public List<Profile> searchProfiles(String query) {
        if (query == null) throw new IllegalArgumentException("Query cannot be null");
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
                    user = getProfileByUsername(data.getString("username"))
                            .orElseThrow(() -> new IllegalStateException("Wrong username or password"));
                    // Check password
                    if (user.checkPassword(data.getString("password"))) {
                        request.setUser(user.getUUID());
                        request.respond(new DataMap().with("uuid", user.getUUID()));
                    } else {
                        throw new IllegalStateException("Wrong username or password");
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
            request.respondWithError(e.getMessage());
        }
    }
}