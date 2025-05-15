package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;

import java.util.*;

public class ProfileManager {
    // Lokal cache af profiler
    private Map<Long, Profile> profiles = new HashMap<>();

    private long currentUserId = -1;
    private ChatClient client = ChatClient.getInstance();

    public long signUp(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("SIGN_UP", new DataMap()
                .with("username", username)
                .with("password", password)));

        ClientMessage res = client.waitingForReply("ProfileManager signUp");

        currentUserId = res.getData().getLong("uuid");

        return res.getData().getLong("uuid");
    }

    public void logout() {
        this.currentUserId = -1;
        // TODO: Bed serveren om at logge ud
    }

    public long login(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("LOG_IN", new DataMap()
                .with("username", username)
                .with("password", password)));

        ClientMessage res = client.waitingForReply("ProfileManager login");

        currentUserId = res.getData().getLong("uuid");

        return res.getData().getLong("uuid");
    }

    public Profile fetchProfile(long userId) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILE", new DataMap()
                .with("uuid", Long.toString(userId))));

        ClientMessage res = client.waitingForReply("ProfileManager getProfile");

        // Gem profil i cache
        Profile profile = Profile.fromData(res.getData().getMap("profile"));
        profiles.put(profile.getUUID(), profile);

        return profiles.get(profile.getUUID());
    }

    public Profile getProfile(long userId) throws ServerError {
        // Hent fra cache
        if (profiles.containsKey(userId)) {
            return profiles.get(userId);
        }

        // Anmod server om profilen
        return fetchProfile(userId);
    }

    public Profile getCurrentUserProfile() throws ServerError {
        client.sendMessage(new ClientMessage("GET_CURRENT_PROFILE", new DataMap()));

        ClientMessage res = client.waitingForReply("ProfileManager getCurrentUserProfile");

        return Profile.fromData(res.getData().getMap("profile"));
    }

    public List<Profile> getProfiles(List<Long> profiles) throws ServerError {
        // TODO: Bruger ikke cache, men er heller ikke brugt endnu så who cares
        client.sendMessage(new ClientMessage("GET_PROFILES", new DataMap()
                .with("profiles", profiles)));

        ClientMessage reply = client.waitingForReply("ProfileManager getProfiles");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    public List<Profile> searchProfiles(String query) throws ServerError {
        client.sendMessage(new ClientMessage("SEARCH_PROFILES", new DataMap()
                .with("query", query)));

        ClientMessage reply = client.waitingForReply("ProfileManager searchProfiles");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    public long getCurrentUserUUID() {
        return this.currentUserId;
    }
}