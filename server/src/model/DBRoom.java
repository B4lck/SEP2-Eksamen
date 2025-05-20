package model;

import model.statemachine.AdministratorState;
import model.statemachine.MutedUser;
import model.statemachine.UserStateId;
import utils.DataMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DBRoom implements Room {
    private long roomId;
    private String name;
    private ArrayList<RoomUser> users = new ArrayList<>();
    private String color;

    private Model model;

    public DBRoom(long roomId, Model model) {
        this.roomId = roomId;
        this.model = model;

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room WHERE id = ?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            ResultSet result = statement.getResultSet();
            if (result.next()) {
                this.color = result.getString("color");
                this.name = result.getString("name");
            } else {
                throw new IllegalStateException("Rummet findes ikke");
            }

            statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id = ?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            ResultSet res = statement.getResultSet();

            while (res.next()) {
                users.add(new RoomUser(
                        res.getLong("profile_id"),
                        UserStateId.fromString(res.getString("state")),
                        res.getLong("latest_read_message"),
                        res.getString("nickname")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public DBRoom(long roomId, String name, Model model) {
        this.name = name;
        this.roomId = roomId;
        this.model = model;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public List<Long> getUsers() {
        return users.stream().map(RoomUser::getId).toList();
    }

    @Override
    public void addUser(long userToAdd, long addedByUser) {
        if (!isAdmin(addedByUser))
            throw new IllegalStateException("Brugeren har ikke tilladelse til at tilføje brugere til dette chatrum");
        if (isInRoom(userToAdd)) throw new IllegalStateException("Brugeren er allerede i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_user (room_id, profile_id, state) VALUES (?,?,?)");
            statement.setLong(1, roomId);
            statement.setLong(2, userToAdd);
            statement.setString(3, UserStateId.REGULAR.getStateId());
            statement.executeUpdate();

            users.add(new RoomUser(userToAdd, UserStateId.REGULAR, 0, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUser(long user, long removedByUser) {
        if (!isAdmin(removedByUser) && user != removedByUser)
            throw new IllegalStateException("Brugeren har ikke tilladelse til at fjerne brugere fra dette chatrum");
        if (!isInRoom(user)) throw new IllegalStateException("Brugeren er ikke i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM room_user WHERE room_id = ? AND profile_id = ?");
            statement.setLong(1, roomId);
            statement.setLong(2, user);
            statement.executeUpdate();

            users.removeIf(ru -> ru.getId() == user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("name", getName())
                .with("chatroomId", getRoomId())
                .with("users", users.stream().map(RoomUser::getData).toList())
                .with("color", color)
                .with("latestActivity", getLatestActivity());
    }

    @Override
    public boolean isInRoom(long user) {
        return users.stream().anyMatch(ru -> ru.getId() == user);
    }

    @Override
    public void setName(String name, long changedByUser) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Rummet må ikke have et tomt navn");
        if (!isAdmin(changedByUser))
            throw new IllegalStateException("Brugeren har ikke tilladelse til at ændre på navnet");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET name=? WHERE id = ?");
            statement.setString(1, name);
            statement.setLong(2, roomId);
            statement.executeUpdate();
            this.name = name;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void muteUser(long userId, long byUser) {
        if (!isAdmin(byUser)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            RoomUser roomUser = getUser(userId);
            roomUser.getState().mute();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, userId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unmuteUser(long userId, long byUser) {
        if (!isAdmin(byUser)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            RoomUser roomUser = getUser(userId);
            roomUser.getState().unmute();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, userId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMuted(long userId) {
        return getUser(userId).getState() instanceof MutedUser;
    }

    @Override
    public void promoteUser(long userId, long promotedByUser) {
        if (!isAdmin(promotedByUser)) throw new IllegalStateException("Brugeren har ikke tilladelse forfremme brugere");
        try (var connection = Database.getConnection()) {
            RoomUser roomUser = getUser(userId);
            roomUser.getState().promote();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, userId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void demoteUser(long userId, long promotedByUser) {
        if (!isAdmin(promotedByUser)) throw new IllegalStateException("Brugeren har ikke tilladelse");
        try (var connection = Database.getConnection()) {
            RoomUser roomUser = getUser(userId);
            roomUser.getState().demote();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, userId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setNicknameOfUser(long userId, String nickname) {
        if (nickname == null || nickname.isEmpty()) throw new IllegalArgumentException("Ulovligt kaldenavn");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET nickname=? WHERE room_id=? AND profile_id=?");
            statement.setString(1, nickname);
            statement.setLong(2, roomId);
            statement.setLong(3, userId);
            if (statement.executeUpdate() == 0)
                throw new IllegalStateException("Brugeren enten findes ikke, eller er ikke i rummet");
            getUser(userId).setNickname(nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeNicknameFromUser(long user) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET nickname = ? WHERE profile_id = ? AND room_id = ?");
            statement.setNull(1, Types.NULL);
            statement.setLong(2, user);
            statement.setLong(3, roomId);
            if (statement.executeUpdate() == 0)
                throw new IllegalStateException("Brugeren enten findes ikke, eller er ikke i rummet");
            getUser(user).setNickname(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAdmin(long userId) {
        return getUser(userId).getState() instanceof AdministratorState;
    }

    @Override
    public RoomUser getUser(long userId) {
        return users.stream().filter(ru -> ru.getId() == userId).findAny().orElseThrow();
    }

    @Override
    public void editColor(String color) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET color=? WHERE id=?");
            statement.setString(1, color);
            statement.setLong(2, roomId);
            statement.executeUpdate();

            this.color = color;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public long getLatestActivity() {
        List<Message> firstMessages = model.getMessages().getMessages(getRoomId(), 1);
        return firstMessages.isEmpty() ? 0 : firstMessages.getFirst().getDateTime();
    }

    @Override
    public void addAdminUser(long user) {
        if (isInRoom(user)) throw new IllegalStateException("Brugeren er allerede i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_user (room_id, profile_id, state) VALUES (?,?,?)");
            statement.setLong(1, roomId);
            statement.setLong(2, user);
            statement.setString(3, UserStateId.ADMIN.getStateId());
            statement.executeUpdate();

            users.add(new RoomUser(user, UserStateId.ADMIN, 0, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
