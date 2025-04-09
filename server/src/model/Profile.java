package model;

public interface Profile {
    long getUUID();
    String getUsername();
    void setUsername(String username);
    boolean checkPassword(String password);
    void setPassword(String password);
}
