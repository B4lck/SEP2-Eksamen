package model;

import utils.DataMap;

public class RoomUser {
    private long userId;
    private String state;
    private long latestReadMessage;
    private String nickname;

    public RoomUser(long userId, String state, long latestReadMessage, String nickname) {
        this.userId = userId;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
        this.nickname = nickname;
    }

    public long getUserId() {
        return userId;
    }

    public String getState() {
        return state;
    }

    public long getLatestReadMessage() {
        return latestReadMessage;
    }

    public static RoomUser fromData(DataMap data) {
        return new RoomUser(
                data.getLong("id"),
                data.getString("state"),
                data.getLong("latestReadMessage"),
                data.getString("nickname")
        );
    }

    public void setRead(long messageId) {
        latestReadMessage = messageId;
    }

    public String getNickname() {
        return nickname;
    }
}
