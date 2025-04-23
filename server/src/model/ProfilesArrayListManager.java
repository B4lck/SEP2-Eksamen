package model;

import mediator.ClientMessage;
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
    public void handleMessage(ServerMessage message) {
        Profile user;

        try {
            switch (message.getType()) {
                // Sign up
                case "SIGN_UP":
                    ServerMessage<SignUpRequest> signUpRequest = message;
                    // Check if username is taken
                    try {
                        getProfileByUsername(signUpRequest.getObject().username());
                    } catch (IllegalArgumentException e) {
                        message.respond(new ClientMessage<>("ERROR", "Username is already taken"));
                        return;
                    }
                    // Create user
                    user = new ArrayListProfile(signUpRequest.getObject().username(), signUpRequest.getObject().password());
                    addProfile(user);
                    // Respond with uuid
                    message.respond(new ClientMessage<>("SIGN_UP", new SignUpResponse(user.getUUID())));
                    // Log user in
                    message.setUser(user.getUUID());
                    break;
                // Log in
                case "LOG_IN":
                    ServerMessage<LogInRequest> logInRequest = message;
                    // Check if user exists
                    try {
                        user = getProfileByUsername(logInRequest.getObject().username());
                    } catch (IllegalArgumentException e) {
                        message.respond(new ClientMessage<>("ERROR", "Wrong username or password"));
                        return;
                    }
                    // Check password
                    if (user.checkPassword(logInRequest.getObject().password())) {
                        message.setUser(user.getUUID());
                        message.respond(new ClientMessage<>("LOG_IN", new LogInResponse(user.getUUID())));
                    } else {
                        message.respond(new ClientMessage<>("ERROR", "Wrong username or password"));
                    }
                    break;
                // Get profile
                case "GET_PROFILE":
                    ServerMessage<GetProfileRequest> getProfileRequest = message;
                    message.respond(new ClientMessage<>("GET_PROFILE", new GetProfileResponse(getProfile(getProfileRequest.getObject().uuid()))));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage<>("ERROR", e.getMessage()));
        }
    }
}

record SignUpRequest(String username, String password) {
}

record SignUpResponse(long uuid) {
}

record LogInRequest(String username, String password) {
}

record LogInResponse(long uuid) {
}

record GetProfileRequest(long uuid) {
}

record GetProfileResponse(Profile profile) {
}