package model;

import mediator.ClientMessage;
import mediator.ServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomsArrayListManager implements ChatRooms {
    private ArrayList<ChatRoom> chatRooms;
    private Model model;

    public ChatRoomsArrayListManager(Model model) {
        chatRooms = new ArrayList<>();
        this.model = model;
    }

    @Override
    public ChatRoom createRoom(String name, long user) {
        ChatRoom chatRoom = new ArrayListChatRoom(name, user);
        chatRooms.add(chatRoom);
        model.getChat().sendSystemMessage(chatRoom.getRoomId(), model.getProfiles().getProfile(user).getUsername() + " oprettede " + name + "!");
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
        model.getChat().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " tilføjede " + model.getProfiles().getProfile(newUser).getUsername() + " til chatten!");
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
                    message.respond(new ClientMessage("GET_ROOM", Map.of("room", getRoom(Long.parseLong((String) message.getData().get("room")), message.getUser()).getData())));
                    break;
                case "GET_MY_ROOMS":
                    ArrayList<Map<String, Object>> rooms = new ArrayList<>();
                    for (ChatRoom room : getParticipatingRooms(message.getUser())) {
                        rooms.add(room.getData());
                    }
                    message.respond(new ClientMessage("GET_ROOMS", Map.of("rooms", rooms)));
                    break;
                case "ADD_USER":
                    addUser(Long.parseLong((String) message.getData().get("room")), Long.parseLong((String) message.getData().get("user")), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of()));
                    break;
                case "REMOVE_USER":
                    removeUser(Long.parseLong((String) message.getData().get("room")), Long.parseLong((String) message.getData().get("user")), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of()));
                    break;
                case "UPDATE_ROOM_NAME":
                    setName(Long.parseLong((String) message.getData().get("room")), (String) message.getData().get("name"), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of()));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }

    @Override
    public void removeUser(long chatroom, long user, long adminUser) {
        ChatRoom chatRoom = getRoomFromId(chatroom);
        chatRoom.removeUser(user, adminUser);
        model.getChat().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " fjernede " + model.getProfiles().getProfile(user).getUsername() + " fra chatten!");
    }

    @Override
    public void setName(long chatroom, String name, long adminUser) {
        ChatRoom chatRoom = getRoomFromId(chatroom);
        chatRoom.setName(name, adminUser);
        model.getChat().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " omdøbte chatten til " + name + "!");
    }
}
