package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ArrayListChatRoom implements ChatRoom {
    private long admin;
    private String name;
    private long chatRoomId;
    private ArrayList<Long> users;

    public ArrayListChatRoom(String name, long user) {
        this.admin = user;
        this.name = name;
        this.chatRoomId = new Random().nextLong();
        this.users = new ArrayList<>();
        users.add(user);
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
    public long[] getUsers() {
        long[] temp = new long[users.size()];
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i) != null)
                temp[i] = users.get(i);
        }
        return temp;
    }

    @Override
    public void addUser(long userToAdd, long addedByUser) {
        if (isInRoom(userToAdd)) throw new RuntimeException("User is already in the room");
        if (addedByUser != admin) throw new RuntimeException("User does not have permission to add users");
        users.add(userToAdd);
    }

    @Override
    public Map<String, Object> getData() {
        // Jeg har ingen ide om hvad der skal være her?!??!
        return Map.of(
                "admin", Long.toString(admin),
                "name", name,
                "chatroomId", Long.toString(chatRoomId),
                "users", users.stream().map((e) -> Long.toString(e)).toList());
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isInRoom(long user) {
        for (long _user : users) {
            if (_user == user)
                return true;
        }
        return false;
    }

    @Override
    public void removeUser(long user, long adminUser) {
        if (!isInRoom(user)) throw new RuntimeException("User is not in the room");
        if (adminUser != admin) throw new RuntimeException("User does not have permission to remove users");
        users.remove(user);
    }
}
