package model;

public class Profile {
    private String username;
    private long uuid;
    private String password;

    public Profile(String username, String password, long uuid) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public long getUUID() {
        return uuid;
    }

    public String getPassword() {
        return password;
    }
}
