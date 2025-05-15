package model;

import model.statemachine.AdministratorState;
import model.statemachine.RegularState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoomsArrayListManagerTest {

    private Model model;
    private Profile user1;
    private Profile user2;
    private Profile user3;
    private Room room;

    @BeforeEach
    void init() throws SQLException {
        Database.startTesting();

        model = new ChatModel();
        user1 = model.getProfiles().createProfile("Mazen", "1234");
        user2 = model.getProfiles().createProfile("TykkeBalck", "6789");
        user3 = model.getProfiles().createProfile("Malthe", "1234");
        room = model.getRooms().createRoom("test", user1.getUUID());
    }

    @AfterEach
    void tearDown() throws SQLException {
        Database.endTesting();
    }

    /**
     * Opret et rum med gyldig navn og bruger
     */
    @org.junit.jupiter.api.Test
    void createRoomNormal() {

        var room = model.getRooms().createRoom("test", user1.getUUID());

        assertEquals(room.getName(), "test");
    }

    /**
     * Opret et rum med null navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom(null, user1.getUUID()));
    }

    /**
     * Opret et rum med tomt navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom("", user1.getUUID()));
    }

    /**
     * Opret rum med en bruger der ikke findes
     */
    @Test
    void creatRoomWithNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().createRoom("Baby", 123));
    }

    /**
     * Hent et rum der findes, og som brugeren er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoom() {
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getRoomId(), room.getRoomId());
    }

    /**
     * Hent et rum der findes, og som brugeren ikke er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoomNoAccess() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(room.getRoomId(), user2.getUUID()));
    }

    /**
     * Hent et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void getNonexistingRoom() {
        if (room.getRoomId() == 123)
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(124, user1.getUUID()));
        else
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(123, user1.getUUID()));
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
        var room2 = model.getRooms().createRoom("test2", user2.getUUID());
        var room3 = model.getRooms().createRoom("test", user2.getUUID());
        room3.addUser(user1.getUUID(), user2.getUUID());
        var room4 = model.getRooms().createRoom("test4", user2.getUUID());
        room4.addUser(user1.getUUID(), user2.getUUID());

        var participatingRooms = model.getRooms().getParticipatingRooms(user1.getUUID());

        assertEquals(participatingRooms.size(), 3);
    }

    /**
     * Hent rum som en bruger som findes, og som ikke er medlem af nogle rum
     */
    @Test
    void getRoomWithoutBeingMember() {
        assertEquals(0, model.getRooms().getParticipatingRooms(user2.getUUID()).size());
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUserAlreadyInRoom() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void addUserToNonExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(69, user2.getUUID(), user1.getUUID()));
    }

    /**
     * Tilføj en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void addUserRoomNonAdmin() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Tilføj en bruger til rummet som ikke eksistere
     */
    @Test
    void addNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), 123, user1.getUUID()));

        assertEquals(1, room.getUsers().size());
    }

    /**
     * Tilføj en bruger til et rum, som ikke deltager af rummet
     */
    @Test
    void addUserWithoutBeingInRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user3.getUUID()));
        assertEquals(1, room.getUsers().size());
    }

    /**
     * Tilføje en bruger der ikke findes til et rum
     */
    @Test
    void addNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), 123, user1.getUUID()));
    }

    /**
     * Tilføj en bruger der er allerede tilføjet
     */
    @Test
    void addExistingUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID()));
        assertEquals(2, room.getUsers().size());
    }

    /**
     * Fjern en bruger fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Fjern en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void removeUserToNonExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(69, user2.getUUID(), user1.getUUID()));
    }

    /**
     * Fjern en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void removeUserNonAdmin() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        var user3 = model.getProfiles().createProfile("jørn123", "456");
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user3.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Fjern en bruger fra et rum som bruger som ikke findes
     */
    @Test
    void removeNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), 123, user1.getUUID()));

    }

    /**
     * Fjern en administrator fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeAdminUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user1.getUUID(), user2.getUUID()));

        assertEquals(room.getUsers().size(), 2);
    }

    /**
     * Fjern en bruger fra et rum, som ikke deltager af rummet
     */
    @Test
    void removeUserWithoutBeingInRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user3.getUUID()));
        assertEquals(1, room.getUsers().size());
    }

    /**
     * Fjern en bruger der ikke findes til et rum
     */
    @Test
    void removeNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), 123, user1.getUUID()));
    }

    /**
     * Fjern en bruger som ikke er deltager i rummet
     */
    @Test
    void removeNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeUser(room.getRoomId(), user3.getUUID(), user1.getUUID()));
        assertEquals(1, room.getUsers().size());
    }

    /**
     * Fjern sig selv fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeSelf() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());

        model.getRooms().removeUser(room.getRoomId(), user2.getUUID(), user2.getUUID());

        assertEquals(room.getUsers().size(), 1);
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomName() {
        model.getRooms().setName(room.getRoomId(), "test2", user1.getUUID());

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test2");
    }

    /**
     * Ændre navn på chatrum, ikke admin
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameNonAdmin() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "test2", user2.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til null
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToNull() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), null, user1.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn til ""
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), "", user1.getUUID()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn, som ikke deltager af rummet
     */
    @Test
    void changeRoomNameNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "GRR", user3.getUUID()));
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn, som ikke eksisterende bruger
     */
    @Test
    void changeRoomNameNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "GRR", 123224));
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUUID()).getName(), "test");
    }

    /**
     * Ændre navn på et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void changeNonExistingRoomName() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(123, "", user1.getUUID()));
    }

    /**
     * Muter en bruger i et chatrum
     */
    @Test
    void muteUser() {
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
        room.addUser(user2.getUUID(), user1.getUUID());
        room.muteUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Unmuter en unmuted bruger
     */
    @Test
    void unmuteUnmutedUser() {
        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * En bruger muter dem selv
     */
    @Test
    void muteThemself() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUUID(), user1.getUUID()));
    }

    /**
     * Muter en bruger uden tilladelse til at mute
     */
    @Test
    void muteWithoutPermission() {
        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Unmuter en bruger uden tilladelse
     */
    @Test
    void unmuteWithoutPermission() {
        room.addUser(user2.getUUID(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Muter en bruger der ikke er i chatrummet
     */
    @Test
    void muteUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Unmuter en bruger der ikke er i chatrummet
     */
    @Test
    void unmuteUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Mute en bruger som er admin
     */
    @Test
    void muteAdmin() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Unmute en bruger som er admin
     */
    @Test
    void unmuteAdmin() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Mute en bruger ikke deltager i rummet
     */
    @Test
    void muteNotMemberUser() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Unmute en bruger ikke deltager i rummet
     */
    @Test
    void unmuteNotMemberUser() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), user1.getUUID()));
    }

    /**
     * Mute en bruger som en bruger der ikke findes
     */
    @Test
    void muteMemberWithoutBeingExisting() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUUID(), 1234));
    }

    /**
     * Unmute en bruger som en bruger der ikke findes
     */
    @Test
    void unmuteMemberWithoutBeingExisting() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUUID(), 1234));
    }

    /**
     * Mute en bruger der ikke findes
     */
    @Test
    void muteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(1234, user1.getUUID()));
    }

    /**
     * Unmute en bruger der ikke findes
     */
    @Test
    void unmuteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(1234, user1.getUUID()));
    }

    /**
     * Forfremmer en bruger der ikke findes
     */
    @Test
    void promoteNotExistingUser() {
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(1234,user1.getUUID()));
    }

    /**
     * Degradere en bruger der ikke findes
     */
    @Test
    void demoteNotExistingUser() {
        assertThrows(IllegalStateException.class, ()-> room.demoteUser(1234, user1.getUUID()));
    }

    /**
     * Forfrommer en bruger, som en bruger der ikke findes.
     */
    @Test
    void promoteByNotExistingUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(user2.getUUID(), 1234));
        assertEquals(RegularState.class ,room.getUser(user2.getUUID()).getState().getClass());
    }

    /**
     * Degradere en bruger, som en bruger der ikke findes.
     */
    @Test
    void demoteByNotExistingUser() {
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, ()-> room.demoteUser(user2.getUUID(), 1234));
        assertEquals(RegularState.class ,room.getUser(user2.getUUID()).getState().getClass());
    }

    /**
     * Froremmer en bruger der ikke findes i rum
     */
    @Test
    void promoteNotExistingMember() {
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(user3.getUUID(), user1.getUUID()));
    }

    /**
     * Degradere en bruger der ikke findes i rum
     */
    @Test
    void demoteNotExistingMember() {
        assertThrows(IllegalStateException.class, ()-> room.demoteUser(user3.getUUID(), user1.getUUID()));
    }

    /**
     * Forfremmer en bruger i rummet
     */
    @Test
    void promoteUser(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        room.promoteUser(user2.getUUID(), user1.getUUID());
        assertEquals(AdministratorState.class ,room.getUser(user2.getUUID()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet
     */
     @Test
    void demoteUser(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        room.promoteUser(user2.getUUID(), user1.getUUID());
        room.demoteUser(user2.getUUID(), user1.getUUID());
        assertEquals(RegularState.class ,room.getUser(user2.getUUID()).getState().getClass());
    }

    /**
     * Forfremmer en admin
     */
    @Test
    void promoteAdmin(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        room.promoteUser(user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(user1.getUUID(), user2.getUUID()));
    }

    /**
     * Degradere en admin
     */
    @Test
    void demoteAdmin(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        room.promoteUser(user2.getUUID(), user1.getUUID());
        room.demoteUser(user1.getUUID(), user2.getUUID());
        assertEquals(RegularState.class ,room.getUser(user1.getUUID()).getState().getClass());
    }

    /**
     * Forfremmer en bruger i rummet, som ikke-deltager af rummet
     */
    @Test
    void promoteUserWithoutBeingMember(){
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(user1.getUUID(), user3.getUUID()));
        assertEquals(AdministratorState.class ,room.getUser(user1.getUUID()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet, som ikke-deltager af rummet
     */
    @Test
    void demoteUserWithoutBeingMember(){
        assertThrows(IllegalStateException.class, ()-> room.demoteUser(user1.getUUID(), user3.getUUID()));
        assertEquals(AdministratorState.class ,room.getUser(user1.getUUID()).getState().getClass());
    }

    /**
     * Forfremmer en bruger i rummet, som ikke-admin af rummet
     */
    @Test
    void promoteUserWithoutBeingAdmin(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, ()-> room.promoteUser(user1.getUUID(), user2.getUUID()));
        assertEquals(AdministratorState.class ,room.getUser(user1.getUUID()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet, som ikke-admin af rummet
     */
    @Test
    void demoteUserWithoutBeingAdmin(){
        model.getRooms().addUser(room.getRoomId(), user2.getUUID(), user1.getUUID());
        assertThrows(IllegalStateException.class, ()-> room.demoteUser(user1.getUUID(), user2.getUUID()));
        assertEquals(AdministratorState.class ,room.getUser(user1.getUUID()).getState().getClass());
    }

}
