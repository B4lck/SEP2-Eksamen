package mediator;

public class ClientMessage<T> {
    private String type;
    private String error;
    private T object;
    private long authenticatedAsUser;

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
