package model;

import java.util.Map;

public class Profile {
    private String username;
    private long uuid;

    public Profile(String username, long uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public static Profile fromData(Map<String, Object> data) {
        return new Profile((String) data.get("username"), Long.parseLong((String) data.get("uuid")));
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return uuid;
    }
}
