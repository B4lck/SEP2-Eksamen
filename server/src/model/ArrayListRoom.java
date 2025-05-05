package model;

import model.statemachine.AdministratorState;
import utils.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArrayListRoom implements Room {
    private String name;
    private long chatRoomId;
    private List<RoomUser> users;

    public ArrayListRoom(String name, long userId) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be null or blank");

        this.name = name;
        this.chatRoomId = new Random().nextLong();
        this.users = new ArrayList<>();

        var adminUser = new RoomUser(userId);

        adminUser.setState(new AdministratorState(adminUser));

        users.add(adminUser);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getRoomId() {
        return chatRoomId;
    }

    @Override
    public List<Long> getUsers() {
        return users.stream().map(RoomUser::getId).toList();
    }

    @Override
    public void addUser(long userToAdd, long addedByUser) {
        if (isInRoom(userToAdd)) throw new IllegalStateException("User is already in the room");
        if (!isUserAdmin(addedByUser)) throw new IllegalStateException("User does not have permission to add users");
        users.add(new RoomUser(userToAdd));
    }

    @Override
    public DataMap getData() {
        // Jeg har ingen ide om hvad der skal være her?!??!
        return new DataMap()
                .with("name", name)
                .with("chatroomId", chatRoomId)
                .with("users", users.stream().map(RoomUser::getId).toList());
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isInRoom(long user) {
        for (RoomUser _user : users) {
            if (_user.getId() == user)
                return true;
        }
        return false;
    }

    @Override
    public void removeUser(long user, long adminUser) {
        if (!isInRoom(user)) throw new IllegalStateException("User is not in the room");
        if (user == adminUser) {
            users.remove(getUserFromUserId(user));
            return;
        }
        if (!isUserAdmin(adminUser)) throw new IllegalStateException("User does not have permission to remove users");
        if (isUserAdmin(user)) throw new IllegalStateException("User cannot be removed");
        users.remove(getUserFromUserId(user));
    }

    @Override
    public void setName(String name, long changedByUser) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be null or blank");
        if (!isUserAdmin(changedByUser)) throw new IllegalStateException("User does not have permission to change name");
        this.name = name;
    }

    public RoomUser getUserFromUserId(long userId) {
        for (RoomUser _user : users) {
            if (_user.getId() == userId)
                return _user;
        }
        throw new IllegalStateException("User does not exist in chatroom '" + name + "'");
    }

    public boolean isUserAdmin(long userId) {
        return getUserFromUserId(userId).getState() instanceof AdministratorState;
    }
}
