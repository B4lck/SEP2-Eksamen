package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProfileManager {
    private ArrayList<Profile> profiles = new ArrayList<>();
    private long currentUserId = -1;

    private ChatClient client = ChatClient.getInstance();

    public long signUp(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("SIGN_UP", Map.of("username", username, "password", password)));

        ClientMessage res = client.waitingForReply("SIGN_UP");

        return Long.parseLong((String) res.getData().get("uuid"));
    }

    public void logout() {
        this.currentUserId = -1;
        // TODO: Bed serveren om at logge ud
    }

    public long login(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("LOG_IN", Map.of("username", username, "password", password)));

        ClientMessage res = client.waitingForReply("LOG_IN");

        return Long.parseLong((String) res.getData().get("uuid"));
    }

    public Profile getProfile(long id) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILE", Map.of("uuid", Long.toString(id))));

        ClientMessage res = client.waitingForReply("GET_PROFILE");

        return Profile.fromData((Map<String, Object>) res.getData().get("profile"));
    }

    public Profile getCurrentUserProfile() throws ServerError {
        client.sendMessage(new ClientMessage("GET_CURRENT_PROFILE", Collections.emptyMap()));

        ClientMessage res = client.waitingForReply("GET_PROFILE");

        return Profile.fromData((Map<String, Object>) res.getData().get("profile"));
    }

    public List<Profile> getProfiles(List<Long> profiles) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILES", Map.of("profiles", profiles.toArray())));

        ClientMessage reply = client.waitingForReply("GET_PROFILES");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (Map<String, Object> profile : (ArrayList<Map<String, Object>>) reply.getData().get("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    public List<Profile> searchProfiles(String query) throws ServerError {
        client.sendMessage(new ClientMessage("SEARCH_PROFILES", Map.of("query", query)));

        ClientMessage reply = client.waitingForReply("GET_PROFILES");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (Map<String, Object> profile : (ArrayList<Map<String, Object>>) reply.getData().get("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }
}