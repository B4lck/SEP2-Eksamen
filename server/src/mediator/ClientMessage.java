package mediator;

public class ClientMessage<T> {
    public String type;
    public String error;
    public T object;
    public long authenticatedAsUser;

    public ClientMessage(String error) {
        this.type = "ERROR";
        this.error = error;
    }

    public ClientMessage(String type, T object) {
        this.type = type;
        this.object = object;
    }

    public void setAuthenticatedUser(long userId) {
        authenticatedAsUser = userId;
    }
}
