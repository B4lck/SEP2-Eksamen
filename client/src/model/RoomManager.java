package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;

public class RoomManager {
    private ChatClient client = ChatClient.getInstance();

    public List<Room> getChatRooms() throws ServerError {
        client.sendMessage(new ClientMessage("GET_MY_ROOMS", new DataMap()));
        var reply = client.waitingForReply("RoomManager getChatRooms");

        ArrayList<Room> chatRooms = new ArrayList<>();
        for (var room : reply.getData().getMapArray("rooms")) {
            chatRooms.add(Room.fromData(room));
        }

        return chatRooms;
    }

    public Room getChatRoom(long chatroom) throws ServerError {
        client.sendMessage(new ClientMessage("GET_ROOM", new DataMap()
                .with("room", chatroom)));

        var reply = client.waitingForReply("RoomManager getChatRoom");

        return Room.fromData(reply.getData().getMap("room"));
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
}
