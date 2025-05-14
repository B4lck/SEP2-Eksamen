package model;

import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Room {
    private String name;
    private long roomId;
    private List<RoomUser> users;

    public Room(String name, long roomId, List<RoomUser> users) {
        this.name = name;
        this.roomId = roomId;
        this.users = new ArrayList<>(users);
    }

    public void update(Room room) {
        this.name = room.name;
        this.users.clear();
        this.users.addAll(room.users);
    }

    public static Room fromData(DataMap message) {
        return new Room(
                message.getString("name"),
                message.getLong("chatroomId"),
                message.getMapArray("users").stream().map(RoomUser::fromData).toList()
        );
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }

    public List<RoomUser> getUsers() {
        return users;
    }

    public Optional<RoomUser> getUser(long userId) {
        return users.stream().filter(user -> user.getUserId() == userId).findAny();
    }
}