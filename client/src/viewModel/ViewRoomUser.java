package viewModel;

public class ViewRoomUser {

    private long userId;
    private String name;
    private String nickname;
    private String state;
    private long latestReadMessage;

    public ViewRoomUser(long userId, String name, String nickname, String state, long latestReadMessage) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDisplayName() {
        return getNickname().isBlank() ? getName() : getNickname();
    }

    public String getState() {
        return state;
    }

    public long getLatestReadMessage() {
        return latestReadMessage;
    }
}
