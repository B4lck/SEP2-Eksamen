package mediator;

/**
 * En server message, er en besked fra klienten til serveren.
 *
 * @param <T> Typen af objekt i beskeden
 */
public class ServerMessage<T> {
    public String type;
    public T object;
    public ClientHandler handler;

    public ServerMessage(String type, T object) {
        this.type = type;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public T getObject() {
        return object;
    }

    public void setHandler(ClientHandler handler) {
        if (handler != null) throw new IllegalStateException("Handleren er allerede sat");
        this.handler = handler;
    }

    public long getUser() {
        return handler.getAuthenticatedUser();
    }

    public void setUser(long userId) {
        handler.setAuthenticatedUser(userId);
    }

    public void respond(ClientMessage message) {
        if (handler == null) throw new IllegalStateException("Du kan kun køre respond på server messages modtager fra klienten.");

        handler.sendMessage(message);
    }
}
