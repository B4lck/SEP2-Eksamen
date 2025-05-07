package model;

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
        model.addHandler(this);
    }

    @Override
    public Room createRoom(String name, long userId) {
        Room chatRoom = new ArrayListRoom(name, userId);
        chatRooms.add(chatRoom);

        model.getMessages().sendSystemMessage(chatRoom.getRoomId(), model.getProfiles().getProfile(userId).map(Profile::getUsername).orElse("En bruger") + " oprettede " + name + "!");

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

        model.getMessages().sendSystemMessage(chatroom,
                model.getProfiles().getProfile(adminUser).map(Profile::getUsername).orElse("En bruger")
                        + " tilføjede "
                        + model.getProfiles().getProfile(newUser).map(Profile::getUsername).orElse("en bruger")
                        + " fra chatten!");
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
    public void handleRequest(ServerRequest request) {
        try {
            switch (request.getType()) {
                case "CREATE_ROOM":
                    request.respond(new DataMap()
                            .with("room", createRoom(request.getData().getString("name"), request.getUser()).getData()));
                    break;
                case "GET_ROOM":
                    request.respond(new DataMap()
                            .with("room", getRoom(request.getData().getLong("room"), request.getUser()).getData()));
                    break;
                case "GET_MY_ROOMS":
                    ArrayList<DataMap> rooms = new ArrayList<>();
                    for (Room room : getParticipatingRooms(request.getUser())) {
                        rooms.add(room.getData());
                    }
                    request.respond(new DataMap().with("rooms", rooms));
                    break;
                case "ADD_USER":
                    addUser(request.getData().getLong("room"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger tilføjet til rummet");
                    break;
                case "REMOVE_USER":
                    removeUser(request.getData().getLong("room"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger fjernet fra rummet");
                    break;
                case "UPDATE_ROOM_NAME":
                    setName(request.getData().getLong("room"), request.getData().getString("name"), request.getUser());
                    request.respond("Rummets navn blev opdateret");
                    break;
                case "MUTE_USER":
                    muteUser(request.getData().getLong("chatroomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet muted");
                    break;
                case "UNMUTE_USER":
                    unmuteUser(request.getData().getLong("chatroomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet unmuted");
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
        }
    }

    @Override
    public void removeUser(long chatroom, long userToRemove, long adminUser) {
        Room chatRoom = getRoomFromId(chatroom);
        chatRoom.removeUser(userToRemove, adminUser);

        model.getMessages().sendSystemMessage(chatroom,
                model.getProfiles().getProfile(adminUser).map(Profile::getUsername).orElse("En bruger")
                        + " fjernede "
                        + model.getProfiles().getProfile(userToRemove).map(Profile::getUsername).orElse("en bruger")
                        + " fra chatten.");
    }

    @Override
    public void setName(long chatroom, String name, long adminUser) {
        Room chatRoom = getRoomFromId(chatroom);
        chatRoom.setName(name, adminUser);
        model.getMessages().sendSystemMessage(chatroom, model.getProfiles().getProfile(adminUser).map(Profile::getUsername).orElse("En bruger") + " omdøbte chatten til " + name + "!");
    }

    @Override
    public void muteUser(long chatroom, long user, long adminUser) {
        Room room = getRoomFromId(chatroom);
        room.muteUser(user, adminUser);
        model.getMessages().sendSystemMessage(chatroom,
                model.getProfiles().getProfile(adminUser).map(Profile::getUsername).orElse("En bruger")
                        + " muted "
                        + model.getProfiles().getProfile(user).map(Profile::getUsername).orElse("En bruger")
                        + ".");
    }

    @Override
    public boolean doesRoomExits(long chatroom) {
        for (Room room : chatRooms) {
            if (room.getRoomId() == chatroom) return true;
        }
        return false;
    }

    @Override
    public void unmuteUser(long chatroom, long user, long adminUser) {
        Room room = getRoomFromId(chatroom);
        room.unmuteUser(user, adminUser);
        model.getMessages().sendSystemMessage(chatroom,
                model.getProfiles().getProfile(adminUser).map(Profile::getUsername).orElse("En bruger")
                        + " unmuted "
                        + model.getProfiles().getProfile(user).map(Profile::getUsername).orElse("En bruger")
                        + ".");
    }
}
