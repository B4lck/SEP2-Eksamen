package model;

import utils.DataMap;

import java.util.List;

public class Room {
    private String name;
    private long roomId;
    private List<Long> users;

    public Room(String name, long roomId, List<Long> users) {
        this.name = name;
        this.roomId = roomId;
        this.users = users;
    }

    public static Room fromData(DataMap message) {

        return new Room(
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