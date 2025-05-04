package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

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
                    message.respond(new ClientMessage("GET_ROOM", new DataMap()
                            .with("room", createRoom(message.getData().getString("name"), message.getUser()).getData())));
                    break;
                case "GET_ROOM":
                    message.respond(new ClientMessage("GET_ROOM", new DataMap()
                            .with("room", getRoom(message.getData().getLong("room"), message.getUser()).getData())));
                    break;
                case "GET_MY_ROOMS":
                    ArrayList<DataMap> rooms = new ArrayList<>();
                    for (ChatRoom room : getParticipatingRooms(message.getUser())) {
                        rooms.add(room.getData());
                    }
                    message.respond(new ClientMessage("GET_ROOMS", new DataMap().with("rooms", rooms)));
                    break;
                case "ADD_USER":
                    addUser(message.getData().getLong("room"), message.getData().getLong("user"), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", new DataMap()));
                    break;
                case "REMOVE_USER":
                    removeUser(message.getData().getLong("room"), message.getData().getLong("user"), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", new DataMap()));
                    break;
                case "UPDATE_ROOM_NAME":
                    setName(message.getData().getLong("room"), message.getData().getString("name"), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", new DataMap()));
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
