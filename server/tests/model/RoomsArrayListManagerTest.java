package model;

import model.statemachine.MutedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoomsArrayListManagerTest {

    private Model model;
    private Profile user1;
    private Profile user2;
    private Room room;

    @BeforeEach
    void init() {
        model = new ChatModel();
        user1 = model.getProfiles().createProfile("Mazen","1234");
        user2 = model.getProfiles().createProfile("TykkeBalck","6789");
        room = model.getRooms().createRoom("HelloWorld",user1.getUUID());
    }

    /**
     * Opret et rum med gyldig navn og bruger
     */
    @org.junit.jupiter.api.Test
    void createRoomNormal() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertEquals(room.getName(), "test");
    }

    /**
     * Opret et rum med null navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithNullName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom(null, user.getUUID()));
    }

    /**
     * Opret et rum med tomt navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithEmptyName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom("", user.getUUID()));
    }

    /**
     * Opret rum med en bruger der ikke findes
     */
    @Test
    void creatRoomWithNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().createRoom("Baby",123));
    }

    /**
     * Hent et rum der findes, og som brugeren er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user.getUUID()).getRoomId(), room.getRoomId());
    }

    /**
     * Hent et rum der findes, og som brugeren ikke er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoomNoAccess() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(room.getRoomId(), user2.getUUID()));
    }

    /**
     * Hent et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void getNonexistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        if (room.getRoomId() == 123)
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(124, user.getUUID()));
        else
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(123, user.getUUID()));
    }

    /**
     * Hent et rum med en bruger der ikke findes
     */
    @Test
    void getRoomWithNonExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(room.getRoomId(), 1232));
    }

    /**
     * Hent et rum som brugeren deltager i
     */
    @org.junit.jupiter.api.Test
    void getParticipatingRooms() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());
        var room2 = model.getRooms().createRoom("test2", user2.getUUID());
        var room3 = model.getRooms().createRoom("test3", user2.getUUID());
        room3.addUser(user.getUUID(), user2.getUUID());
        var room4 = model.getRooms().createRoom("test4", user2.getUUID());
        room4.addUser(user.getUUID(), user2.getUUID());

        var participatingRooms = model.getRooms().getParticipatingRooms(user.getUUID());

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

        var room = model.getRooms().createRoom("test", user.getUUID());
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUserAlreadyInRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void addUserToNonExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(69, user2.getUUID(), user.getUUID()));
    }

    /**
     * Tilføj en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void addUserRoomNonAdmin() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Fjern en bruger fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeUser() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());
        model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Fjern en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void removeUserToNonExistingRoom() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(69, user2.getUUID(), user.getUUID()));
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

        var room = model.getRooms().createRoom("test", user.getUUID());

        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user3.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Fjern en administrator fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeAdminUser() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Fjern sig selv fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeSelf() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user.getUUID());

        model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user2.getUUID());

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        model.getRooms().setName(room.getRoomId(), "test2", user.getUUID());

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test2");
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameNonAdmin() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");
        var user2 = model.getProfiles().createProfile("hello2", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "test2", user2.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til null
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToNull() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), null, user.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til ""
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToEmptyString() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        var room = model.getRooms().createRoom("test", user.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), "", user.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn på et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void changeNonExistingRoomName() {
        var model = new ChatModel();

        var user = model.getProfiles().createProfile("hello", "1");

        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(123, "", user.getUUID()));
    }

    /**
     * Muter en bruger i et chatrum
     */
    @Test
    void muteUser() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());
        room.muteUser(user2.getUUID(), user1.getUUID());

        // Skal throw, fordi user2 skal ikke kunne skrive
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUUID()));
    }

    /**
     * Unmuter en muted bruger i et chatrum
     */
    @Test
    void unmuteUser() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());
        // mute først
        room.muteUser(user2.getUUID(), user1.getUUID());
        // unmute igen
        room.unmuteUser(user2.getUUID(), user1.getUUID());

        // Skal ikke kaste
        assertDoesNotThrow(() -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUUID()));
    }

    /**
     * Muter en muted bruger
     */
    @Test
    void muteMutedUser() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());
        room.muteUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Unmuter en unmuted bruger
     */
    @Test
    void unmuteUnmutedUser() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * En bruger muter dem selv
     */
    @Test
    void muteThemself() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUUID(), user1.getUUID()));
    }

    /**
     * Muter en bruger uden tilladelse til at mute
     */
    @Test
    void muteWithoutPermission() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Unmuter en bruger uden tilladelse
     */
    @Test
    void unmuteWithoutPermission() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Muter en bruger der ikke er i chatrummet
     */
    @Test
    void muteUserNotInRoom() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Unmuter en bruger der ikke er i chatrummet
     */
    @Test
    void unmuteUserNotInRoom() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test room", user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), user1.getUUID()));
    }
}