package model;

import utils.DataMap;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    private String name;
    private long roomId;
    private List<Long> users;

    public ChatRoom(String name, long roomId, List<Long> users) {
        this.name = name;
        this.roomId = roomId;
        this.users = users;
    }

    public static ChatRoom fromData(DataMap message) {

        return new ChatRoom(
                message.getString("name"),
                message.getLong("chatroomId"),
                message.getLongsArray("users")
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