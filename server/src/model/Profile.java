package model;

import java.util.Map;

public interface Profile {
    long getUUID();
    String getUsername();
    void setUsername(String username);
    boolean checkPassword(String password);
    void setPassword(String password);
    Map<String, Object> getData();
}
