package model;

import mediator.ClientMessage;
import mediator.ServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomsArrayListManager implements ChatRooms {
    private ArrayList<ChatRoom> chatRooms;

    public ChatRoomsArrayListManager() {
        chatRooms = new ArrayList<>();
    }

    @Override
    public ChatRoom createRoom(String name, long user) {
        ChatRoom chatRoom = new ArrayListChatRoom(name, user);
        chatRooms.add(chatRoom);
        return chatRoom;
    }

    @Override
    public ChatRoom getRoom(long roomId, long user) {
        ChatRoom chatRoom = getRoomFromId(roomId);
        if (!chatRoom.isInRoom(user)) throw new RuntimeException("User does not have access to the room");
        return chatRoom;
    }

    @Override
    public List<ChatRoom> getParticipatingRooms(long user) {
        ArrayList<ChatRoom> participatingRooms = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            if (chatRoom.isInRoom(user))
                participatingRooms.add(chatRoom);
        }
        return participatingRooms;
    }

    @Override
    public void addUser(long chatroom, long newUser, long adminUser) {
        ChatRoom chatRoom = getRoomFromId(chatroom);
        chatRoom.addUser(newUser, adminUser);
    }

    private ChatRoom getRoomFromId(long id) {
        ChatRoom chatRoom = null;
        for (ChatRoom _chatRoom : chatRooms) {
            if (_chatRoom.getRoomId() == id) {
                chatRoom = _chatRoom;
                break;
            }
        }
        if (chatRoom == null) throw new RuntimeException("Room does not exist");
        return chatRoom;
    }


    @Override
    public void handleMessage(ServerRequest message) {
        try {
            switch (message.getType()) {
                case "CREATE_ROOM":
                    message.respond(new ClientMessage("GET_ROOM", Map.of("room", createRoom((String) message.getData().get("name"), message.getUser()).getData())));
                    break;
                case "GET_ROOM":
                    message.respond(new ClientMessage("GET_ROOM", Map.of("room", getRoom(Long.parseLong((String) message.getData().get("room")), message.getUser()))));
                    break;
                case "GET_MY_ROOMS":
                    ArrayList<Map<String, Object>> rooms = new ArrayList<>();
                    for (ChatRoom room : getParticipatingRooms(message.getUser())) {
                        rooms.add(room.getData());
                    }
                    message.respond(new ClientMessage("GET_ROOMS", Map.of("rooms", rooms)));
                    break;
                case "ADD_USER":
                    addUser(Long.parseLong((String)message.getData().get("room")), Long.parseLong((String)message.getData().get("user")), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of()));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }
}
