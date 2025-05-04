package model;

import utils.DataMap;

public class Profile {
    private String username;
    private long uuid;

    public Profile(String username, long uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public static Profile fromData(DataMap data) {
        return new Profile(data.getString("username"), data.getLong("uuid"));
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return uuid;
    }
}
