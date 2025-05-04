package mediator;

import utils.DataMap;

import java.util.Map;

public class ClientMessage {
    private String type;
    private String error;
    private Map<String, Object> data;
    private String authenticatedAsUser;
    private boolean broadcast = false;

    public ClientMessage(String type, DataMap data) {
        this.type = type;
        this.data = data;
    }

    public ClientMessage(String error) {
        this.error = error;
    }

    public DataMap getData() {
        return new DataMap(data);
    }

    public Long getAuthenticatedAsUser() {
        return Long.parseLong(authenticatedAsUser);
    }

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean isBroadcast() {
        return broadcast;
    }
}
