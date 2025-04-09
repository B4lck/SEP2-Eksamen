package mediator;

public class ClientMessage<T> {
    private String type;
    private String error;
    private T object;
    private long authenticatedAsUser;

    public ClientMessage(String type, T object) {
        this.type = type;
        this.object = object;
    }

    public ClientMessage(String error) {
        this.error = error;
    }

    public T getObject() {
        return object;
    }

    public long getAuthenticatedAsUser() {
        return authenticatedAsUser;
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
}
