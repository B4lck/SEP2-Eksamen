package viewModel;

public class ViewRoomMember {

    private long userId;
    private String name;
    private String nickname;
    private String state;
    private long latestReadMessage;
    private long lastActive;
    private boolean isBlocked;

    public ViewRoomMember(long userId, String name, String nickname, String state) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.latestReadMessage = 0;
        this.lastActive = 0;
        this.isBlocked = false;
    }

    public ViewRoomMember(long userId, String name, String nickname, String state, long latestReadMessage, long lastActive) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
        this.lastActive = lastActive;
        this.isBlocked = false;
    }

    public ViewRoomMember(long userId, String name, String nickname, String state, long latestReadMessage, long lastActive, boolean isBlocked) {
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

    private String newState = null;

    public String getNewState() {
        return newState;
    }

    public void setNewState(String state) {
        this.newState = state;
    }
}
