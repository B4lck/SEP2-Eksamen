package model;

import model.statemachine.AdministratorState;
import model.statemachine.MutedUser;
import model.statemachine.UserStateId;
import utils.DataMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBRoom implements Room {
    private long roomId;
    private String name;
    private ArrayList<RoomMember> members = new ArrayList<>();
    private String color;
    private String font;

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
                this.font = result.getString("font");
                this.name = result.getString("name");
            } else {
                throw new IllegalStateException("Rummet findes ikke");
            }

            statement = connection.prepareStatement("SELECT * FROM room_user WHERE room_id = ?");
            statement.setLong(1, roomId);
            statement.executeQuery();
            ResultSet res = statement.getResultSet();

            while (res.next()) {
                members.add(new RoomMember(
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public List<Long> getMembers() {
        return members.stream().map(RoomMember::getUserId).toList();
    }

    @Override
    public void addMember(long addUserId, long adminUserId) {
        if (!isAdmin(adminUserId))
            throw new IllegalStateException("Brugeren har ikke tilladelse til at tilføje brugere til dette chatrum");
        if (isMember(addUserId)) throw new IllegalStateException("Brugeren er allerede i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_user (room_id, profile_id, state) VALUES (?,?,?)");
            statement.setLong(1, roomId);
            statement.setLong(2, addUserId);
            statement.setString(3, UserStateId.REGULAR.getStateId());
            statement.executeUpdate();

            members.add(new RoomMember(addUserId, UserStateId.REGULAR, 0, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeMember(long removeUserId, long adminUserId) {
        if (!isAdmin(adminUserId) && removeUserId != adminUserId)
            throw new IllegalStateException("Brugeren har ikke tilladelse til at fjerne brugere fra dette chatrum");
        if (!isMember(removeUserId)) throw new IllegalStateException("Brugeren er ikke i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM room_user WHERE room_id = ? AND profile_id = ?");
            statement.setLong(1, roomId);
            statement.setLong(2, removeUserId);
            statement.executeUpdate();

            members.removeIf(member -> member.getUserId() == removeUserId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("name", getName())
                .with("roomId", getRoomId())
                .with("membersIds", members.stream().map(RoomMember::getData).toList())
                .with("color", getColor())
                .with("font", getFont())
                .with("latestActivityTime", getLatestActivity())
                .with("latestMessageId", getLatestMessage());
    }

    @Override
    public boolean isMember(long userId) {
        return members.stream().anyMatch(member -> member.getUserId() == userId);
    }

    @Override
    public void setName(String name, long adminUserId) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Rummet må ikke have et tomt navn");
        if (!isAdmin(adminUserId))
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
    public void muteUser(long muteUserId, long adminUserId) {
        if (!isAdmin(adminUserId)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            RoomMember roomUser = getMember(muteUserId);
            roomUser.getState().mute();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, muteUserId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unmuteUser(long unmuteUserId, long adminUserId) {
        if (!isAdmin(adminUserId)) throw new IllegalStateException("Brugeren har ikke tilladelse til at mute brugere");
        try (var connection = Database.getConnection()) {
            RoomMember roomUser = getMember(unmuteUserId);
            roomUser.getState().unmute();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, unmuteUserId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMuted(long userId) {
        return getMember(userId).getState() instanceof MutedUser;
    }

    @Override
    public void promoteUser(long promoteUserId, long adminUserId) {
        if (!isAdmin(adminUserId)) throw new IllegalStateException("Brugeren har ikke tilladelse forfremme brugere");
        try (var connection = Database.getConnection()) {
            RoomMember roomUser = getMember(promoteUserId);
            roomUser.getState().promote();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, promoteUserId);
            statement.setLong(3, roomId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void demoteUser(long demoteUserId, long adminUserId) {
        if (!isAdmin(adminUserId)) throw new IllegalStateException("Brugeren har ikke tilladelse");
        try (var connection = Database.getConnection()) {
            RoomMember roomUser = getMember(demoteUserId);
            roomUser.getState().demote();

            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET state=? WHERE profile_id=? AND room_id=?");
            statement.setString(1, roomUser.getState().getStateAsString());
            statement.setLong(2, demoteUserId);
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
            getMember(userId).setNickname(nickname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeNicknameFromUser(long userId) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET nickname = ? WHERE profile_id = ? AND room_id = ?");
            statement.setNull(1, Types.NULL);
            statement.setLong(2, userId);
            statement.setLong(3, roomId);
            if (statement.executeUpdate() == 0)
                throw new IllegalStateException("Brugeren enten findes ikke, eller er ikke i rummet");
            getMember(userId).setNickname(null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAdmin(long userId) {
        return getMember(userId).getState() instanceof AdministratorState;
    }

    @Override
    public RoomMember getMember(long userId) {
        return members.stream().filter(ru -> ru.getUserId() == userId).findAny().orElseThrow(() -> new IllegalStateException("Brugeren er ikke medlem af dette rum"));
    }

    @Override
    public void setColor(String color) {
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
    public String getColor() {
        return this.color;
    }

    @Override
    public long getLatestActivity() {
        List<Message> firstMessages = model.getMessages().getMessages(getRoomId(), 1);
        return firstMessages.isEmpty() ? 0 : firstMessages.getFirst().getDateTime();
    }

    @Override
    public long getLatestMessage() {
        List<Message> firstMessages = model.getMessages().getMessages(getRoomId(), 1);
        return firstMessages.isEmpty() ? -1 : firstMessages.getFirst().getMessageId();
    }

    @Override
    public void addAdminMember(long userId) {
        if (isMember(userId)) throw new IllegalStateException("Brugeren er allerede i rummet");

        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_user (room_id, profile_id, state) VALUES (?,?,?)");
            statement.setLong(1, roomId);
            statement.setLong(2, userId);
            statement.setString(3, UserStateId.ADMIN.getStateId());
            statement.executeUpdate();

            members.add(new RoomMember(userId, UserStateId.ADMIN, 0, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setFont(String font) {
        try (var connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET font=? WHERE id=?");
            statement.setString(1, font);
            statement.setLong(2, roomId);
            statement.executeUpdate();

            this.font = font;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getFont() {
        return this.font;
    }

    @Override
    public void setLatestReadMessage(long messageId, long userId) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE room_user SET latest_read_message = ? WHERE room_id = ? AND profile_id = ?");
            statement.setLong(1, messageId);
            statement.setLong(2, this.getRoomId());
            statement.setLong(3, userId);
            statement.executeUpdate();

            getMember(userId).setLatestReadMessage(messageId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
