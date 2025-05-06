package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;

public class RoomsArrayListManager implements Rooms {
    private ArrayList<Room> chatRooms;
    private Model model;

    public RoomsArrayListManager(Model model) {
        chatRooms = new ArrayList<>();
        this.model = model;
    }

    @Override
    public Room createRoom(String name, long user) {
        Room chatRoom = new ArrayListRoom(name, user);
        chatRooms.add(chatRoom);
        model.getMessages().sendSystemMessage(chatRoom.getRoomId(), model.getProfiles().getProfile(user).getUsername() + " oprettede " + name + "!");
        return chatRoom;
    }

    @Override
    public Room getRoom(long roomId, long user) {
        Room chatRoom = getRoomFromId(roomId);
        if (!chatRoom.isInRoom(user)) throw new IllegalStateException("User does not have access to the room");
        return chatRoom;
    }

    @Override
    public List<Room> getParticipatingRooms(long user) {
        ArrayList<Room> participatingRooms = new ArrayList<>();
        for (Room chatRoom : chatRooms) {
            if (chatRoom.isInRoom(user))
                participatingRooms.add(chatRoom);
        }
        return participatingRooms;
    }

    @Override
    public void addUser(long chatroom, long newUser, long adminUser) {
        Room chatRoom = getRoomFromId(chatroom);
        chatRoom.addUser(newUser, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " tilføjede " + model.getProfiles().getProfile(newUser).getUsername() + " til chatten!");
    }

    private Room getRoomFromId(long id) {
        Room chatRoom = null;
        for (Room _chatRoom : chatRooms) {
            if (_chatRoom.getRoomId() == id) {
                chatRoom = _chatRoom;
                break;
            }
        }
        if (chatRoom == null) throw new IllegalStateException("Rummet findes ikke");
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
                    for (Room room : getParticipatingRooms(message.getUser())) {
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
                case "MUTE_USER":
                    muteUser(message.getData().getLong("chatroomId"), message.getData().getLong("userId"), message.getUser());
                    message.respond(new ClientMessage("SUCCESS", new DataMap()));
                    break;
                case "UNMUTE_USER":
                    unmuteUser(message.getData().getLong("chatroomId"), message.getData().getLong("userId"), message.getUser());
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
        Room chatRoom = getRoomFromId(chatroom);
        chatRoom.removeUser(user, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " fjernede " + model.getProfiles().getProfile(user).getUsername() + " fra chatten!");
    }

    @Override
    public void setName(long chatroom, String name, long adminUser) {
        Room chatRoom = getRoomFromId(chatroom);
        chatRoom.setName(name, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " omdøbte chatten til " + name + "!");
    }

    @Override
    public void muteUser(long chatroom, long user, long adminUser) {
        Room room = getRoomFromId(chatroom);
        room.muteUser(user, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " muted " + model.getProfiles().getProfile(user).getUsername() + ".");
    }

    @Override
    public void unmuteUser(long chatroom, long user, long adminUser) {
        Room room = getRoomFromId(chatroom);
        room.unmuteUser(user, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).getUsername() + " unmuted " + model.getProfiles().getProfile(user).getUsername() + ".");

    }
}
