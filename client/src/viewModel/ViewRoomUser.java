package viewModel;

public class ViewRoomUser {

    private long userId;
    private String name;
    private String nickname;
    private String state;
    private long latestReadMessage;
    private long lastActive;
    private boolean isBlocked;

    public ViewRoomUser(long userId, String name, String nickname, String state, long latestReadMessage, long lastActive) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
        this.lastActive = lastActive;
        this.isBlocked = false;
    }

    public ViewRoomUser(long userId, String name, String nickname, String state, long latestReadMessage, long lastActive, boolean isBlocked) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
        this.lastActive = lastActive;
        this.isBlocked = isBlocked;
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
        return getNickname() == null || getNickname().isBlank() ? getName() : getNickname();
    }

    public String getState() {
        return state;
    }

    public long getLatestReadMessage() {
        return latestReadMessage;
    }

    public long getLastActive() {
        return lastActive;
    }

    public boolean isBlocked() {
        return isBlocked;
    }
}
