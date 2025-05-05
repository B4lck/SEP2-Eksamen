package model;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomsArrayListManagerTest {

    /**
     * Opret et rum med gyldig navn og bruger
     */
    @org.junit.jupiter.api.Test
    void createRoomNormal() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertEquals(room.getName(), "test");
    }

    /**
     * Opret et rum med null navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithNullName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalArgumentException.class, () -> model.getChatRooms().createRoom(null, user.getUUID()));
    }

    /**
     * Opret et rum med tomt navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithEmptyName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalArgumentException.class, () -> model.getChatRooms().createRoom("", user.getUUID()));
    }

    /**
     * Hent et rum der findes, og som brugeren er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertEquals(model.getChatRooms().getRoom(room.getRoomId(), user.getUUID()).getRoomId(), room.getRoomId());
    }

    /**
     * Hent et rum der findes, og som brugeren ikke er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoomNoAccess() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().getRoom(room.getRoomId(), user2.getUUID()));
    }

    /**
     * Hent et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void getNonexistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        if (room.getRoomId() == 123)
            assertThrows(IllegalStateException.class, () -> model.getChatRooms().getRoom(124, user.getUUID()));
        else
            assertThrows(IllegalStateException.class, () -> model.getChatRooms().getRoom(123, user.getUUID()));
    }

    /**
     * Hent et rum som brugeren deltager i
     */
    @org.junit.jupiter.api.Test
    void getParticipatingRooms() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());
        var room2 = model.getChatRooms().createRoom("test2", user2.getUUID());
        var room3 = model.getChatRooms().createRoom("test3", user2.getUUID());
        room3.addUser(user.getUUID(), user2.getUUID());
        var room4 = model.getChatRooms().createRoom("test4", user2.getUUID());
        room4.addUser(user.getUUID(), user2.getUUID());

        var participatingRooms = model.getChatRooms().getParticipatingRooms(user.getUUID());

        assertEquals(participatingRooms.size(), 3);
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUser() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());
        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertEquals(room.getUsers().length, 2);
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUserAlreadyInRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());
        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID()));

        assertEquals(room.getUsers().length, 2);
    }

    /**
     * Tilføj en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void addUserToNonExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().addUser(69, user2.getUUID(), user.getUUID()));
    }

    /**
     * Tilføj en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void addUserRoomNonAdmin() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().length, 1);
    }

    /**
     * Fjern en bruger fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeUser() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());
        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());
        model.getChatRooms().removeUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertEquals(room.getUsers().length, 1);
    }

    /**
     * Fjern en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void removeUserToNonExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().removeUser(69, user2.getUUID(), user.getUUID()));
    }

    /**
     * Fjern en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void removeUserNonAdmin() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");
        var user3 = model.getProfiles().createProfile("hello3", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().removeUser(room.getRoomId(), user2.getUUID(), user3.getUUID()));

        assertEquals(room.getUsers().length, 2);
    }

    /**
     * Fjern en administrator fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeAdminUser() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().removeUser(room.getRoomId(), user.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().length, 2);
    }

    /**
     * Fjern sig selv fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeSelf() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        model.getChatRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        model.getChatRooms().removeUser(room.getRoomId(), user2.getUUID(), user2.getUUID());

        assertEquals(room.getUsers().length, 1);
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        model.getChatRooms().setName(room.getRoomId(), "test2", user.getUUID());

        assertEquals(model.getChatRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test2");
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameNonAdmin() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().setName(room.getRoomId(), "test2", user2.getUUID()));

        assertEquals(model.getChatRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til null
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToNull() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getChatRooms().setName(room.getRoomId(), null, user.getUUID()));

        assertEquals(model.getChatRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til ""
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToEmptyString() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getChatRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getChatRooms().setName(room.getRoomId(), "", user.getUUID()));

        assertEquals(model.getChatRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn på et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void changeNonExistingRoomName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalStateException.class, () -> model.getChatRooms().setName(123, "", user.getUUID()));
    }
}