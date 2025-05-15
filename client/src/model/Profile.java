package model;

import utils.DataMap;

public class Profile {
    private String username;
    private long uuid;
    private long lastActive;

    public Profile(String username, long uuid, long lastActive) {
        this.username = username;
        this.uuid = uuid;
        this.lastActive = lastActive;
    }

    public static Profile fromData(DataMap data) {
        return new Profile(data.getString("username"), data.getLong("uuid"), data.getLong("lastActive"));
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return uuid;
    }

    public long getLastActive() {
        return lastActive;
    }
}
