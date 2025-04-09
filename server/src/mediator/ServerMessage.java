package mediator;

public class ServerMessage<T> {
    private String type;
    private T object;
    private ClientHandler handler;

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

    public ClientHandler getHandler() {
        return handler;
    }

    public void setHandler(ClientHandler handler) {
        this.handler = handler;
    }

    public long getUser() {
        return 0;
    }

    public void respond(ServerMessage message) {

    }
}
