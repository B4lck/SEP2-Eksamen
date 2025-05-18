package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;
import utils.PropertyChangeSubject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomManager implements PropertyChangeSubject, PropertyChangeListener {
    private PropertyChangeSupport property = new PropertyChangeSupport(this);

    private List<Room> rooms = new ArrayList<>();

    private ChatClient client = ChatClient.getInstance();

    public RoomManager() {
        client.addListener(this);
    }

    public List<Room> getChatRooms() throws ServerError {
        client.sendMessage(new ClientMessage("GET_MY_ROOMS", new DataMap()));
        var reply = client.waitingForReply("RoomManager getChatRooms");

        ArrayList<Room> chatRooms = new ArrayList<>();
        for (var room : reply.getData().getMapArray("rooms")) {
            chatRooms.add(addOrSetRoom(Room.fromData(room)));
        }

        return chatRooms;
    }

    public Room getChatRoom(long chatroom) throws ServerError {
        client.sendMessage(new ClientMessage("GET_ROOM", new DataMap()
                .with("room", chatroom)));

        var reply = client.waitingForReply("RoomManager getChatRoom");

        return addOrSetRoom(Room.fromData(reply.getData().getMap("room")));
    }

    private Room addOrSetRoom(Room room) {
        Optional<Room> existingRoom = rooms.stream().filter(r2 -> r2.getRoomId() == room.getRoomId()).findAny();
        if (existingRoom.isPresent()) {
            existingRoom.get().update(room);
            return existingRoom.get();
        } else {
            rooms.add(room);
            return room;
        }
    }

    public void addUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("ADD_USER", new DataMap()
                .with("room", chatroom)
                .with("user", userId)));
        client.waitingForReply("RoomManager addUser");
    }

    public long createRoom(String name) throws ServerError {
        client.sendMessage(new ClientMessage("CREATE_ROOM", new DataMap()
                .with("name", name)));

        return Room.fromData(client.waitingForReply("RoomManager createRoom").getData().getMap("room")).getRoomId();
    }

    public void removeUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_USER", new DataMap()
                .with("room", chatroom)
                .with("user", userId)));

        client.waitingForReply("RoomManager removeUser");
    }

    public void setName(long chatroom, String name) throws ServerError {
        client.sendMessage(new ClientMessage("UPDATE_ROOM_NAME", new DataMap()
                .with("room", chatroom)
                .with("name", name)));

        client.waitingForReply("SUCCESS");
    }

    public void muteUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("MUTE_USER", new DataMap()
                .with("chatroomId", chatroom)
                .with("user", userId)));

        client.waitingForReply("SUCCESS");
    }

    public void unmuteUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("UNMUTE_USER", new DataMap()
                .with("chatroomId", chatroom)
                .with("user", userId)));

        client.waitingForReply("SUCCESS");
    }

    public void promoteUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("PROMOTE_USER", new DataMap()
                .with("chatroomId", chatroom)
                .with("user", userId)));

        client.waitingForReply("SUCCESS");
    }

    public void demoteUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("DEMOTE_USER", new DataMap()
                .with("chatroomId", chatroom)
                .with("user", userId)));

        client.waitingForReply("SUCCESS");
    }

    public Optional<Room> getRoom(long roomId) {
        return rooms.stream().filter(room -> room.getRoomId() == roomId).findAny();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var message = (ClientMessage) evt.getNewValue();
        var request = message.getData();

        switch (message.getType().toUpperCase()) {
            case "READ_MESSAGE":
                try {
                    Room room = getRoom(request.getLong("roomId")).orElseThrow();
                    RoomUser user = room.getUser(request.getLong("userId")).orElseThrow();
                    user.setRead(request.getLong("messageId"));
                    property.firePropertyChange("READ_UPDATE", room.getRoomId(), user);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Gør intet
                }
                break;
        }
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    public void editNickname(long chatroomId, long userId, String nickname) throws ServerError {
        client.sendMessage(new ClientMessage("SET_NICKNAME", new DataMap()
                .with("chatroomId", chatroomId)
                .with("userId", userId)
                .with("nickname", nickname)));

        client.waitingForReply("SUCCESS");
    }

    public void removeNickname(long chatroomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_NICKNAME", new DataMap()
                .with("chatroomId", chatroomId)
                .with("userId", userId)));

        client.waitingForReply("SUCCESS");
    }

    public String getNicknameOf(long chatroomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("GET_NICKNAME", new DataMap()
                .with("chatroomId", chatroomId)
                .with("userId", userId)));

        var reply = client.waitingForReply("NICKNAME");
        return reply.getData().getString("nickname");
    }
}
