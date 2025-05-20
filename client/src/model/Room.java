package model;

import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Room {
    private String name;
    private long roomId;
    private List<RoomMember> members;
    private long latestActivityTime;
    private long latestMessageId;
    private String color;
    private String font;

    public Room(String name, long roomId, List<RoomMember> members, long lastActivityTime, long latestMessageId, String color, String font) {
        this.name = name;
        this.roomId = roomId;
        this.members = new ArrayList<>(members);
        this.latestActivityTime = lastActivityTime;
        this.latestMessageId = latestMessageId;
        this.color = color;
        this.font = font;
    }

    public void update(Room room) {
        this.name = room.name;
        this.members.clear();
        this.members.addAll(room.members);
        this.latestActivityTime = room.latestActivityTime;
        this.color = room.color;
        this.font = room.font;
    }

    public static Room fromData(DataMap message) {
        return new Room(
                message.getString("name"),
                message.getLong("roomId"),
                message.getMapArray("membersIds").stream().map(RoomMember::fromData).toList(),
                message.getLong("latestActivityTime"),
                message.getLong("latestMessageId"),
                message.getString("color"),
                message.getString("font")
        );
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }

    public List<RoomMember> getMembers() {
        return members;
    }

    public String getColor() {
        return color;
    }
    public String getFont(){
        return font;
    }

    public Optional<RoomMember> getUser(long userId) {
        return members.stream().filter(user -> user.getUserId() == userId).findAny();
    }

    public long getLatestActivityTime() {
        return latestActivityTime;
    }

    public long getLatestMessageId() {
        return latestMessageId;
    }

    public void setLatestActivityTime(long latestActivityTime) {
        this.latestActivityTime = latestActivityTime;
    }

    public void setLatestMessageId(long latestMessageId) {
        this.latestMessageId = latestMessageId;
    }
}