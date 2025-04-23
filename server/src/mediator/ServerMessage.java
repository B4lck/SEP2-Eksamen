package mediator;

import java.util.Map;

/**
 * En server message, er en besked fra klienten til serveren.
 */
public class ServerMessage {
    private String type;
    private Map<String, Object> data;
    private ClientHandler handler;

    public ServerMessage(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setHandler(ClientHandler handler) {
        if (this.handler != null) throw new IllegalStateException("Handleren er allerede sat");
        this.handler = handler;
    }

    public long getUser() {
        return handler.getAuthenticatedUser();
    }

    public void setUser(long userId) {
        handler.setAuthenticatedUser(userId);
    }

    public void respond(ClientMessage message) {
        System.out.println(handler);
        if (handler == null) throw new IllegalStateException("Du kan kun køre respond på server messages modtager fra klienten.");

        handler.sendMessage(message);
    }
}
