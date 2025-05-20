package model;

import mediator.Broadcast;
import mediator.ServerRequest;
import utils.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsDBManager implements Rooms {
    private final Model model;
    private final PropertyChangeSupport property = new PropertyChangeSupport(this);

    private final Map<Long, Room> rooms = new HashMap<>();

    public RoomsDBManager(Model model) {
        this.model = model;
        this.model.addHandler(this);
    }

    @Override
    public Room createRoom(String name, long user) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Rummet må ikke have et tomt navn");
        if (model.getProfiles().getProfile(user).isEmpty()) throw new IllegalStateException("Brugeren findes ikke");

        try (Connection connection = Database.getConnection()) {
            // Opret rummet
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);

            statement.executeUpdate();

            ResultSet res = statement.getGeneratedKeys();
            if (!res.next())
                throw new RuntimeException();

            Room room = new DBRoom(res.getLong(1), model);

            room.addAdminUser(user);

            rooms.put(room.getRoomId(), room);

            fireRoomChangedBroadcast(room.getRoomId());

            return room;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Room getRoom(long roomId, long user) {
        Room room = getRoom(roomId);
        if (!room.isInRoom(user)) throw new IllegalStateException("Du har ikke adgang til dette rum");
        return room;
    }

    @Override
    public Room getRoom(long roomId) {
        if (!rooms.containsKey(roomId)) rooms.put(roomId, new DBRoom(roomId, model));
        return rooms.get(roomId);
    }

    @Override
    public List<Room> getParticipatingRooms(long user) {
        List<Room> rooms = new ArrayList<>();
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT room_id FROM room_user WHERE profile_id=?");
            statement.setLong(1, user);
            statement.executeQuery();

            ResultSet res = statement.getResultSet();
            while (res.next()) {
                rooms.add(getRoom(res.getLong("room_id"), user));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rooms;
    }

    @Override
    public void addUser(long chatroom, long newUser, long adminUser) {
        if (model.getProfiles().getProfile(newUser).isEmpty()) throw new IllegalStateException("Brugeren findes ikke");

        Room room = getRoom(chatroom, adminUser);
        room.addUser(newUser, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void removeUser(long chatroom, long kickedUser, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.removeUser(kickedUser, adminUser);

        property.firePropertyChange("KICKED_OUT_OF_ROOM", null,
                new Broadcast(new DataMap().with("roomId", chatroom), kickedUser));

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void setName(long chatroom, String name, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.setName(name, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void muteUser(long chatroom, long user, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.muteUser(user, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public boolean doesRoomExists(long chatroom) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room WHERE id=?");
            statement.setLong(1, chatroom);
            statement.executeQuery();
            return statement.getResultSet().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editColor(long chatroom, long user, String color) {
        Room room = getRoom(chatroom, user);
        room.editColor(color);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void unmuteUser(long chatroom, long user, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.unmuteUser(user, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void promoteUser(long chatroom, long user, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.promoteUser(user, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void demoteUser(long chatroom, long user, long adminUser) {
        Room room = getRoom(chatroom, adminUser);
        room.demoteUser(user, adminUser);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void setNicknameOfUser(long chatroom, long user, String nickname) {
        Room room = getRoom(chatroom, user);
        room.setNicknameOfUser(user, nickname);

        fireRoomChangedBroadcast(chatroom);
    }

    @Override
    public void removeNicknameOfUser(long chatroom, long user) {
        Room room = getRoom(chatroom, user);
        room.removeNicknameFromUser(user);

        fireRoomChangedBroadcast(chatroom);
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
                    muteUser(request.getData().getLong("chatroomId"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger er blevet muted");
                    break;
                case "UNMUTE_USER":
                    unmuteUser(request.getData().getLong("chatroomId"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger er blevet unmuted");
                    break;
                case "PROMOTE_USER":
                    promoteUser(request.getData().getLong("chatroomId"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger er blevet promoted");
                    break;
                case "DEMOTE_USER":
                    demoteUser(request.getData().getLong("chatroomId"), request.getData().getLong("user"), request.getUser());
                    request.respond("Bruger er blevet degraderet");
                    break;
                case "SET_NICKNAME":
                    setNicknameOfUser(request.getData().getLong("chatroomId"), request.getData().getLong("userId"), request.getData().getString("nickname"));
                    request.respond("Brugeren har fået ændret sit kaldenavn");
                    break;
                case "REMOVE_NICKNAME":
                    removeNicknameOfUser(request.getData().getLong("chatroomId"), request.getData().getLong("userId"));
                    request.respond("Brugeren har fået fjernet sit kaldenavn");
                    break;
                case "EDIT_COLOR":
                    editColor(request.getData().getLong("chatroomId"), request.getUser(), request.getData().getString("color"));
                    request.respond("Farven er blevet ændret");
                    break;
                case "SET_FONT":
                    setFont(request.getData().getLong("chatroomId"),request.getUser(),request.getData().getString("font"));
                    request.respond("Skrifttypen er blevet ændret");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
        }
    }

    @Override
    public void setFont(long chatroomId, long user, String font) {
        getRoom(chatroomId,user).setFont(font,user);
        fireRoomChangedBroadcast(chatroomId);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    /**
     * Broadcast til alle brugere i rummet, at rummet er blevet opdateret.
     * @param roomId ID'et på rummet
     */
    public void fireRoomChangedBroadcast(long roomId) {
        Room room = getRoom(roomId);
        property.firePropertyChange("ROOM_CHANGED", null,
                new Broadcast(new DataMap().with("room", room.getData()), room.getUsers()));
    }
}
