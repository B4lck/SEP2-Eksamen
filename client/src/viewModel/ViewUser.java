package viewModel;

public class ViewUser {
    private String username;
    private long userId;

    public ViewUser(long userId, String username) {
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public long getUserId() {
        return userId;
    }
}
