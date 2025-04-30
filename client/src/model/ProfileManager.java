package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ProfileManager {
    private ArrayList<Profile> profiles = new ArrayList<>();
    private long currentUserId = -1;

    public ProfileManager() {

    }

    public long signUp(String username, String password) throws ServerError {
        ChatClient.getInstance().sendMessage(new ClientMessage("SIGN_UP", Map.of("username", username, "password", password)));

        ClientMessage res = ChatClient.getInstance().waitingForReply("SIGN_UP");

        return Long.parseLong((String) res.getData().get("uuid"));
    }

    public void logout() {
        this.currentUserId = -1;
        // TODO: Bed serveren om at logge ud
    }

    public long login(String username, String password) throws ServerError {
        ChatClient.getInstance().sendMessage(new ClientMessage("LOG_IN", Map.of("username", username, "password", password)));

        ClientMessage res = ChatClient.getInstance().waitingForReply("LOG_IN");

        return Long.parseLong((String) res.getData().get("uuid"));
    }

    public Profile getProfile(long id) throws ServerError {
        ChatClient.getInstance().sendMessage(new ClientMessage("GET_PROFILE", Map.of("uuid", Long.toString(id))));

        ClientMessage res = ChatClient.getInstance().waitingForReply("GET_PROFILE");

        return Profile.fromData((Map<String, Object>) res.getData().get("profile"));
    }

    public Profile getCurrentUserProfile() throws ServerError {
        ChatClient.getInstance().sendMessage(new ClientMessage("GET_CURRENT_PROFILE", Collections.emptyMap()));

        ClientMessage res = ChatClient.getInstance().waitingForReply("GET_PROFILE");

        return Profile.fromData((Map<String, Object>) res.getData().get("profile"));
    }
}