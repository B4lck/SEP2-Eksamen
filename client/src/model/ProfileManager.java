package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;

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
        client.sendMessage(new ClientMessage("SIGN_UP", new DataMap()
                .with("username", username)
                .with("password", password)));

        ClientMessage res = client.waitingForReply("SIGN_UP");

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

        ClientMessage res = client.waitingForReply("LOG_IN");

        return res.getData().getLong("uuid");
    }

    public Profile getProfile(long id) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILE", new DataMap()
                .with("uuid", Long.toString(id))));

        ClientMessage res = client.waitingForReply("GET_PROFILE");

        return Profile.fromData(res.getData().getMap("profile"));
    }

    public Profile getCurrentUserProfile() throws ServerError {
        client.sendMessage(new ClientMessage("GET_CURRENT_PROFILE", new DataMap()));

        ClientMessage res = client.waitingForReply("GET_PROFILE");

        return Profile.fromData(res.getData().getMap("profile"));
    }

    public List<Profile> getProfiles(List<Long> profiles) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILES", new DataMap()
                .with("profiles", profiles)));

        ClientMessage reply = client.waitingForReply("GET_PROFILES");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    public List<Profile> searchProfiles(String query) throws ServerError {
        client.sendMessage(new ClientMessage("SEARCH_PROFILES", new DataMap()
                .with("query", query)));

        ClientMessage reply = client.waitingForReply("GET_PROFILES");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }
}