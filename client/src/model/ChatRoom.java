package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoom {
    private String name;
    private long roomId;
    private ArrayList<Long> users;

    public ChatRoom(String name, long roomId, ArrayList<Long> users) {
        this.name = name;
        this.roomId = roomId;
        this.users = users;
    }

    public static ChatRoom fromData(Map<String, Object> message) {
        var users = (ArrayList<String>) message.get("users");
        var usersLong = new ArrayList<Long>();
        for (String user : users) {
            usersLong.add(Long.parseLong(user));
        }

        return new ChatRoom(
                (String) message.get("name"),
                Long.parseLong((String) message.get("id")),
                usersLong
        );
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }

    public List<Long> getUsers() {
        return users;
    }
}