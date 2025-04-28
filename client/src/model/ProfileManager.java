package model;

import mediator.ChatClient;
import mediator.ClientMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ProfileManager {
    private ArrayList<Profile> profiles = new ArrayList<>();
    private long currentUserId = -1;

    public ProfileManager() {

    }

    public long signUp(String username, String password) {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("SIGN_UP", Map.of("username", username, "password", password)));

            System.out.println("En besked burde være sendt");

            ClientMessage res = ChatClient.getInstance().waitingForReply("SIGN_UP");

            System.out.println("Kommer beskeden tilbage?");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return ((Double) res.getData().get("uuid")).longValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Profile> getAll() {
        return profiles;
    }

    public void logout() {
        this.currentUserId = -1;
    }

    public long login(String username, String password) {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("LOG_IN", Map.of("username", username, "password", password)));

            ClientMessage res = ChatClient.getInstance().waitingForReply("LOG_IN");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return ((Double) res.getData().get("uuid")).longValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Profile getProfile(long id) {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("GET_PROFILE", Map.of("uuid", id)));

            ClientMessage res = ChatClient.getInstance().waitingForReply("GET_PROFILE");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return (Profile) res.getData().get("profile");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Profile getCurrentUser() {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("GET_CURRENT_PROFILE", Collections.emptyMap()));

            ClientMessage res = ChatClient.getInstance().waitingForReply("GET_PROFILE");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return (Profile) res.getData().get("profile");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}