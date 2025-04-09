package model;

import mediator.ChatClient;
import mediator.ClientMessage;

import java.util.ArrayList;

public class ProfileManager implements BroadcastReceiver {
    private ArrayList<Profile> profiles = new ArrayList<>();
    private long currentUserId = -1;
    private ChatClient client;

    public ProfileManager(ChatClient client) {
        this.client = client;
    }

    public long signUp(String username, String password) {
        try {
            client.sendMessage(new ClientMessage<>("SIGN_UP", new SignUpMessage(username, password)));

            ClientMessage<SignUpResponse> res = client.waitingForReply("SIGN_UP");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return res.getObject().uuid();
        } catch (Exception e) {
            throw new RuntimeException("Det gik noget galt!");
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
            client.sendMessage(new ClientMessage<>("LOG_IN", new LogInMessage(username, password)));

            ClientMessage<LogInResponse> res = client.waitingForReply("LOG_IN");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return res.getObject().uuid();
        } catch (Exception e) {
            throw new RuntimeException("Det gik noget galt!");
        }
    }

    public Profile getProfile(long id) {
        try {
            client.sendMessage(new ClientMessage<>("GET_PROFILE", new GetProfileMessage(id)));

            ClientMessage<GetProfileResponse> res = client.waitingForReply("GET_PROFILE");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return res.getObject().profile();
        } catch (Exception e) {
            throw new RuntimeException("Det gik noget galt!");
        }
    }

    public Profile getCurrentUser() {
        try {
            client.sendMessage(new ClientMessage<>("GET_CURRENT_PROFILE"));

            ClientMessage<GetProfileResponse> res = client.waitingForReply("GET_PROFILE");

            if (res.hasError()) throw new RuntimeException(res.getError());

            return res.getObject().profile();
        } catch (Exception e) {
            throw new RuntimeException("Det gik noget galt!");
        }
    }

    @Override
    public void onBroadcast(ClientMessage message) {
    }
}

record SignUpMessage(String username, String password) {
}

record SignUpResponse(long uuid) {
}

record LogInMessage(String username, String password) {
}

record LogInResponse(long uuid) {
}

record GetProfileMessage(long uuid) {
}

record GetProfileResponse(Profile profile) {
}