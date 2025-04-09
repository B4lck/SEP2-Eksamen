package model;

public class Profile {
    private String username;
    private long uuid;

    public Profile(String username, String password, long uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return uuid;
    }
}
