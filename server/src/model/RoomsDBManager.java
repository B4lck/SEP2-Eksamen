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
import java.util.*;
import java.util.List;

public class RoomsDBManager implements Rooms {
    private final Model model;
    private final PropertyChangeSupport property = new PropertyChangeSupport(this);

    private final Map<Long, Room> rooms = new HashMap<>();

    public RoomsDBManager(Model model) {
        this.model = model;
        this.model.addHandler(this);
    }

    @Override
    public Room createRoom(String name, long userId) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Rummet må ikke have et tomt navn");
        Profile profile = model.getProfiles().getProfile(userId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

        try (Connection connection = Database.getConnection()) {
            // Opret rummet
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);

            statement.executeUpdate();

            ResultSet res = statement.getGeneratedKeys();
            if (!res.next())
                throw new RuntimeException();

            Room room = new DBRoom(res.getLong(1), model);

            room.addAdminMember(userId);

            rooms.put(room.getRoomId(), room);

            fireRoomChangedBroadcast(room.getRoomId());

            model.getMessages().sendSystemMessage(
                    room.getRoomId(),
                    profile.getUsername() + " oprettede chatrummet!"
            );

            return room;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Room getRoom(long roomId, long userId) {
        Room room = getRoom(roomId);
        if (!room.isMember(userId)) throw new IllegalStateException("Du har ikke adgang til dette rum");
        return room;
    }

    @Override
    public Room getRoom(long roomId) {
        if (!rooms.containsKey(roomId)) rooms.put(roomId, new DBRoom(roomId, model));
        return rooms.get(roomId);
    }

    @Override
    public List<Room> getParticipatingRooms(long userId) {
        List<Room> rooms = new ArrayList<>();
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT room_id FROM room_user WHERE profile_id=?");
            statement.setLong(1, userId);
            statement.executeQuery();

            ResultSet res = statement.getResultSet();
            while (res.next()) {
                rooms.add(getRoom(res.getLong("room_id"), userId));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rooms;
    }

    @Override
    public void addMember(long roomId, long addUserId, long adminUserId) {
        Profile toAdd = model.getProfiles().getProfile(addUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.addMember(addUserId, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, admin.getUsername() + " tilføjede " + toAdd.getUsername() + " til chatrummet!");
    }

    @Override
    public void removeMember(long roomId, long removeUserId, long adminUserId) {
        Profile toAdd = model.getProfiles().getProfile(removeUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.removeMember(removeUserId, adminUserId);

        property.firePropertyChange("KICKED_OUT_OF_ROOM", null,
                new Broadcast(new DataMap().with("roomId", roomId), removeUserId));

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, admin.getUsername() + " fjernede " + toAdd.getUsername() + " til chatrummet!");
    }

    @Override
    public void setName(long roomId, String name, long adminUserId) {
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.setName(name, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, admin.getUsername() + " ændrede chatrummets navn til " + name + "!");
    }

    @Override
    public void muteMember(long roomId, long muteUserId, long adminUserId) {
        Profile mutedUser = model.getProfiles().getProfile(muteUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.muteUser(muteUserId, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, mutedUser.getUsername() + " blev muted af " + admin.getUsername() + "!");
    }

    @Override
    public boolean doesRoomExists(long roomId) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room WHERE id=?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            return statement.getResultSet().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasAccessTo(long roomId, long userId) {
        return getRoom(roomId).isMember(userId);
    }

    @Override
    public void setColor(long roomId, long userId, String color) {
        Profile user = model.getProfiles().getProfile(userId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, userId);
        room.setColor(color);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, user.getUsername() + " ændrede farven på chatrummet!");
    }

    @Override
    public void unmuteMember(long roomId, long unmuteUserId, long adminUserId) {
        Profile unmutedUser = model.getProfiles().getProfile(unmuteUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.unmuteUser(unmuteUserId, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, unmutedUser.getUsername() + " blev unmuted af " + admin.getUsername() + "!");
    }

    @Override
    public void promoteMember(long roomId, long promoteUserId, long adminUserId) {
        Profile promotedUser = model.getProfiles().getProfile(promoteUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.promoteUser(promoteUserId, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, promotedUser.getUsername() + " blev forfremmet af " + admin.getUsername() + "!");
    }

    @Override
    public void demoteMember(long roomId, long demoteUserId, long adminUserId) {
        Profile demotedUser = model.getProfiles().getProfile(demoteUserId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));
        Profile admin = model.getProfiles().getProfile(adminUserId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        Room room = getRoom(roomId, adminUserId);
        room.demoteUser(demoteUserId, adminUserId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, demotedUser.getUsername() + " blev degraderet af " + admin.getUsername() + "!");
    }

    @Override
    public void setMemberNickname(long roomId, long userId, String nickname) {
        Profile profile = model.getProfiles().getProfile(userId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

        Room room = getRoom(roomId, userId);
        room.setNicknameOfUser(userId, nickname);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, profile.getUsername() + "s kaldenavn blev ændret til " + nickname + "!");
    }

    @Override
    public void removeMemberNickname(long roomId, long userId) {
        Profile profile = model.getProfiles().getProfile(userId).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

        Room room = getRoom(roomId, userId);
        room.removeNicknameFromUser(userId);

        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, profile.getUsername() + "s kaldenavn blev ændret til " + profile.getUsername() + "!");
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
                            .with("room", getRoom(request.getData().getLong("roomId"), request.getUser()).getData()));
                    break;
                case "GET_MY_ROOMS":
                    ArrayList<DataMap> rooms = new ArrayList<>();
                    for (Room room : getParticipatingRooms(request.getUser())) {
                        rooms.add(room.getData());
                    }
                    request.respond(new DataMap().with("rooms", rooms));
                    break;
                case "ADD_MEMBER":
                    addMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger tilføjet til rummet");
                    break;
                case "REMOVE_MEMBER":
                    removeMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger fjernet fra rummet");
                    break;
                case "UPDATE_ROOM_NAME":
                    setName(request.getData().getLong("roomId"), request.getData().getString("name"), request.getUser());
                    request.respond("Rummets navn blev opdateret");
                    break;
                case "MUTE_MEMBER":
                    muteMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet muted");
                    break;
                case "UNMUTE_MEMBER":
                    unmuteMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet unmuted");
                    break;
                case "PROMOTE_MEMBER":
                    promoteMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet promoted");
                    break;
                case "DEMOTE_MEMBER":
                    demoteMember(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getUser());
                    request.respond("Bruger er blevet degraderet");
                    break;
                case "SET_NICKNAME":
                    setMemberNickname(request.getData().getLong("roomId"), request.getData().getLong("userId"), request.getData().getString("nickname"));
                    request.respond("Brugeren har fået ændret sit kaldenavn");
                    break;
                case "REMOVE_NICKNAME":
                    removeMemberNickname(request.getData().getLong("roomId"), request.getData().getLong("userId"));
                    request.respond("Brugeren har fået fjernet sit kaldenavn");
                    break;
                case "EDIT_COLOR":
                    setColor(request.getData().getLong("roomId"), request.getUser(), request.getData().getString("color"));
                    request.respond("Farven er blevet ændret");
                    break;
                case "SET_FONT":
                    setFont(request.getData().getLong("roomId"), request.getUser(), request.getData().getString("font"));
                    request.respond("Skrifttypen er blevet ændret");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
        }
    }

    @Override
    public void setFont(long roomId, long userId, String font) {
        Profile user = model.getProfiles().getProfile(userId).orElseThrow(() -> new IllegalStateException("Admin findes ikke"));

        getRoom(roomId, userId).setFont(font);
        fireRoomChangedBroadcast(roomId);

        model.getMessages().sendSystemMessage(roomId, user.getUsername() + " ændrede skrifttypen på chatrummet til " + font + "!");
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
     *
     * @param roomId ID'et på rummet
     */
    public void fireRoomChangedBroadcast(long roomId) {
        Room room = getRoom(roomId);
        property.firePropertyChange("ROOM_CHANGED", null,
                new Broadcast(new DataMap().with("room", room.getData()), room.getMembers()));
    }
}
