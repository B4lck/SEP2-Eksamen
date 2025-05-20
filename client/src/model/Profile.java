package model;

import utils.DataMap;

public class Profile {
    private String username;
    private long userId;
    private long lastActive;

    public Profile(String username, long userId, long lastActive) {
        this.username = username;
        this.userId = userId;
        this.lastActive = lastActive;
    }

    public static Profile fromData(DataMap data) {
        return new Profile(data.getString("username"), data.getLong("userId"), data.getLong("lastActive"));
    }

    public String getUsername() {
        return username;
    }

    public long getUserId() {
        return userId;
    }

    public long getLastActive() {
        return lastActive;
    }
}
