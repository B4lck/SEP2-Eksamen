package mediator;

import java.util.Map;

public class ClientMessage {
    public String type;
    public String error;
    public Map<String, Object> data;
    public long authenticatedAsUser;

    public ClientMessage(String error) {
        this.type = "ERROR";
        this.error = error;
    }

    public ClientMessage(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public void setAuthenticatedUser(long userId) {
        authenticatedAsUser = userId;
    }
}
