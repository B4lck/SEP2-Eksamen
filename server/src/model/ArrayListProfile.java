package model;

import java.util.Random;

public class ArrayListProfile implements Profile{
    private String username;
    private String password;
    private long uuid;

    public ArrayListProfile(String username, String password) {
        this.username = username;
        this.password = password;
        this.uuid = new Random().nextLong();
    }


    @Override
    public long getUUID() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean checkPassword(String password) {
        return password.equals(this.password);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
