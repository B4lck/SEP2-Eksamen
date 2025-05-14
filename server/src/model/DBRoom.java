package model;

import model.statemachine.AdministratorState;
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

    public DBRoom(long roomId) {
        this.roomId = roomId;

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT name FROM room WHERE id = " + roomId);
            statement.executeQuery();
            ResultSet result = statement.getResultSet();
            if (result.next()) {
                this.name = result.getString("name");
            } else {
                throw new IllegalStateException("Rummet findes ikke");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public DBRoom(long roomId, String name) {
        this.name = name;
        this.roomId = roomId;
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
        try (var connection = Database.getConnection()) {
            List<Long> users = new ArrayList<>();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            ResultSet res = statement.getResultSet();
            while (res.next()) {
                users.add(res.getLong("profile_id"));
            }

            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataMap getData() {
        try (var connection = Database.getConnection()) {
            List<RoomUser> users = new ArrayList<>();

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            ResultSet res = statement.getResultSet();
            while (res.next()) {
                users.add(new RoomUser(
                        res.getLong("profile_id"),
                        UserStateId.fromString(res.getString("state")),
                        res.getLong("latest_read_message")
                ));
            }

            return new DataMap()
                    .with("name", name)
                    .with("chatroomId", roomId)
                    .with("users", users.stream().map(RoomUser::getData).toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInRoom(long user) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id = ? AND profile_id = ?");
            statement.setLong(1, roomId);
            statement.setLong(2, user);
            statement.executeQuery();
            return statement.getResultSet().next();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void muteUser(long userId, long byUser) {
        if (!isAdmin(byUser)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);

            ResultSet res = statement.executeQuery();
            if (res.next()) {
                RoomUser roomUser = new RoomUser(userId, UserStateId.fromString(res.getString("state")), res.getLong("latest_read_message"));
                roomUser.getState().mute();

                statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
                statement.setString(1, roomUser.getState().getStateAsString());
                statement.setLong(2, userId);
                statement.setLong(3, roomId);
                statement.executeUpdate();
            } else {
                throw new IllegalStateException("Brugeren findes ikke i rummet");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unmuteUser(long userId, long byUser) {
        if (!isAdmin(byUser)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                RoomUser roomUser = new RoomUser(userId, UserStateId.fromString(res.getString("state")), res.getLong("latest_read_message"));
                roomUser.getState().unmute();

                statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
                statement.setString(1, roomUser.getState().getStateAsString());
                statement.setLong(2, userId);
                statement.setLong(3, roomId);
                statement.executeUpdate();
            } else {
                throw new IllegalStateException("Brugeren findes ikke i rummet");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMuted(long userId) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=? AND state=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            statement.setString(3, "Muted");

            return statement.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void promoteUser(long userId, long promotedByUser) {
        if (!isAdmin(promotedByUser)) throw new IllegalStateException("Brugeren har ikke tilladelse forfremme brugere");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                RoomUser roomUser = new RoomUser(userId, UserStateId.fromString(res.getString("state")), res.getLong("latest_read_message"));
                roomUser.getState().promote();

                statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
                statement.setString(1, roomUser.getState().getStateAsString());
                statement.setLong(2, userId);
                statement.setLong(3, roomId);
                statement.executeUpdate();
            } else {
                throw new IllegalStateException("Brugeren findes ikke i rummet");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void demoteUser(long userId, long promotedByUser) {
        if (!isAdmin(promotedByUser)) throw new IllegalStateException("Brugeren har ikke tilladelse");
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                RoomUser roomUser = new RoomUser(userId, UserStateId.fromString(res.getString("state")), res.getLong("latest_read_message"));
                roomUser.getState().demote();

                statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
                statement.setString(1, roomUser.getState().getStateAsString());
                statement.setLong(2, userId);
                statement.setLong(3, roomId);
                statement.executeUpdate();
            } else {
                throw new IllegalStateException("Brugeren findes ikke i rummet");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setNicknameOfUser(long userId, String nickname) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET nickname=? WHERE room_id=? AND profile_id=?");
            statement.setString(1, nickname);
            statement.setLong(2, roomId);
            statement.setLong(3, userId);
            statement.executeUpdate();
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
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNickname(long user) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT nickname FROM room_user WHERE room_id = ? AND profile_id = ?");
            statement.setLong(1, roomId);
            statement.setLong(2, user);
            statement.executeQuery();

            ResultSet result = statement.getResultSet();
            if (result.next())
                return result.getString("nickname");

            throw new IllegalStateException("Brugeren findes ikke i rummet");
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
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id=? AND profile_id=?");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                return new RoomUser(userId, UserStateId.fromString(res.getString("state")), res.getLong("latest_read_message"));
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
