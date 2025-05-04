package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomManager {
    private ChatClient client = ChatClient.getInstance();

    public List<ChatRoom> getChatRooms() throws ServerError {
        client.sendMessage(new ClientMessage("GET_MY_ROOMS", Map.of()));
        var reply = client.waitingForReply("GET_ROOMS");

        ArrayList<ChatRoom> chatRooms = new ArrayList<>();
        for (Map<String, Object> room : (ArrayList<Map<String, Object>>) reply.getData().get("rooms")) {
            chatRooms.add(ChatRoom.fromData(room));
        }

        return chatRooms;
    }

    public ChatRoom getChatRoom(long chatroom) throws ServerError {
        client.sendMessage(new ClientMessage("GET_ROOM", Map.of("room", Long.toString(chatroom))));
        var reply = client.waitingForReply("GET_ROOM");
        return ChatRoom.fromData((Map<String, Object>) reply.getData().get("room"));
    }

    public void addUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("ADD_USER", Map.of("room", Long.toString(chatroom), "user", Long.toString(userId))));
        client.waitingForReply("SUCCESS");
    }

    public long createRoom(String name) throws ServerError {
        client.sendMessage(new ClientMessage("CREATE_ROOM", Map.of("name", name)));
        long reply = ChatRoom.fromData((Map<String, Object>) client.waitingForReply("GET_ROOM").getData().get("room")).getRoomId();
        return reply;
    }
    public void removeUser(long chatroom, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_USER", Map.of("room", Long.toString(chatroom), "user", Long.toString(userId))));
        client.waitingForReply("SUCCESS");
    }

    public void setName(long chatroom, String name) throws ServerError {
        client.sendMessage(new ClientMessage("UPDATE_ROOM_NAME", Map.of("room", Long.toString(chatroom), "name", name)));
        client.waitingForReply("SUCCESS");
    }
}
