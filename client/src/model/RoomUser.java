package model;

import utils.DataMap;

public class RoomUser {
    private long userId;
    private String state;
    private long latestReadMessage;

    public RoomUser(long userId, String state, long latestReadMessage) {
        this.userId = userId;
        this.state = state;
        this.latestReadMessage = latestReadMessage;
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
        return new RoomUser(data.getLong("id"), data.getString("state"), data.getLong("latestReadMessage"));
    }

    public void setRead(long messageId) {
        latestReadMessage = messageId;
    }
}
