package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomManager {
    private ChatClient client = ChatClient.getInstance();

    public List<ChatRoom> getChatRooms() throws ServerError {
        client.sendMessage(new ClientMessage("GET_MY_ROOMS", new DataMap()));
        var reply = client.waitingForReply("GET_ROOMS");

        ArrayList<ChatRoom> chatRooms = new ArrayList<>();
        for (var room : reply.getData().getMapArray("rooms")) {
            chatRooms.add(ChatRoom.fromData(room));
        }

        return chatRooms;
    }

    public ChatRoom getChatRoom(long chatroom) throws ServerError {
        client.sendMessage(new ClientMessage("GET_ROOM", new DataMap()
                .with("room", chatroom)));
        
        var reply = client.waitingForReply("GET_ROOM");
        
        return ChatRoom.fromData(reply.getData().getMap("room"));
    }

    public void addUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("ADD_USER", new DataMap()
                .with("room", chatroom)
                .with("user", userId)));
        client.waitingForReply("SUCCESS");
    }

    public long createRoom(String name) throws ServerError {
        client.sendMessage(new ClientMessage("CREATE_ROOM", new DataMap()
                .with("name", name)));
        
        long reply = ChatRoom.fromData(client.waitingForReply("GET_ROOM").getData().getMap("room")).getRoomId();
        
        return reply;
    }

    public void removeUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_USER", new DataMap()
                .with("room", chatroom)
                .with("user", userId)));
        
        client.waitingForReply("SUCCESS");
    }

    public void setName(long chatroom, String name) throws ServerError {
        client.sendMessage(new ClientMessage("UPDATE_ROOM_NAME", new DataMap()
                .with("room", chatroom)
                .with("name", name)));
        
        client.waitingForReply("SUCCESS");
    }
}
