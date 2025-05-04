package mediator;

import utils.DataMap;

import java.util.Map;

/**
 * En client message, har samme instans-variabler som klassen på klient-siden
 */
public class ClientMessage {
    public String type;
    public String error;
    public Map<String, Object> data;
    public String authenticatedAsUser;
    public boolean broadcast = false;

    public ClientMessage(String error) {
        this.type = "ERROR";
        this.error = error;
    }

    public ClientMessage(String type, DataMap data) {
        this.type = type;
        this.data = data;
    }

    public void setAuthenticatedUser(long userId) {
        authenticatedAsUser = Long.toString(userId);
    }
}
