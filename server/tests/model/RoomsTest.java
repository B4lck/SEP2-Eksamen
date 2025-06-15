package model;

import model.statemachine.AdministratorState;
import model.statemachine.RegularState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoomsTest {

    private Model model;
    private Profile user1;
    private Profile user2;
    private Profile user3;
    private Room room;

    @BeforeEach
    void init() throws SQLException {
        Database.startTesting();

        model = new ChatModel();
        user1 = model.getProfiles().createProfile("Mazen", "12341234");
        user2 = model.getProfiles().createProfile("TykkeBalck", "67891234");
        user3 = model.getProfiles().createProfile("Malthe", "12341234");
        room = model.getRooms().createRoom("test", user1.getUserId());
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

        var room = model.getRooms().createRoom("test", user1.getUserId());

        assertEquals(room.getName(), "test");
    }

    /**
     * Opret et rum med null navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom(null, user1.getUserId()));
    }

    /**
     * Opret et rum med tomt navn
     */
    @org.junit.jupiter.api.Test
    void createRoomWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().createRoom("", user1.getUserId()));
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
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getRoomId(), room.getRoomId());
    }

    /**
     * Hent et rum der findes, og som brugeren ikke er deltager i
     */
    @org.junit.jupiter.api.Test
    void getExistingRoomNoAccess() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(room.getRoomId(), user2.getUserId()));
    }

    /**
     * Hent et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void getNonexistingRoom() {
        if (room.getRoomId() == 123)
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(124, user1.getUserId()));
        else
            assertThrows(IllegalStateException.class, () -> model.getRooms().getRoom(123, user1.getUserId()));
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
        var room2 = model.getRooms().createRoom("test2", user2.getUserId());
        var room3 = model.getRooms().createRoom("test", user2.getUserId());
        room3.addMember(user1.getUserId(), user2.getUserId());
        var room4 = model.getRooms().createRoom("test4", user2.getUserId());
        room4.addMember(user1.getUserId(), user2.getUserId());

        var participatingRooms = model.getRooms().getParticipatingRooms(user1.getUserId());

        assertEquals(participatingRooms.size(), 3);
    }

    /**
     * Hent rum som en bruger som findes, og som ikke er medlem af nogle rum
     */
    @Test
    void getRoomWithoutBeingMember() {
        assertEquals(0, model.getRooms().getParticipatingRooms(user2.getUserId()).size());
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        assertEquals(room.getMembers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum
     */
    @org.junit.jupiter.api.Test
    void addUserAlreadyInRoom() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId()));

        assertEquals(room.getMembers().size(), 2);
    }

    /**
     * Tilføj en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void addUserToNonExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(69, user2.getUserId(), user1.getUserId()));
    }

    /**
     * Tilføj en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void addUserRoomNonAdmin() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user2.getUserId()));

        assertEquals(room.getMembers().size(), 1);
    }

    /**
     * Tilføj en bruger til rummet som ikke eksistere
     */
    @Test
    void addNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), 123, user1.getUserId()));

        assertEquals(1, room.getMembers().size());
    }

    /**
     * Tilføj en bruger til et rum, som ikke deltager af rummet
     */
    @Test
    void addUserWithoutBeingInRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user3.getUserId()));
        assertEquals(1, room.getMembers().size());
    }

    /**
     * Tilføje en bruger der ikke findes til et rum
     */
    @Test
    void addNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), 123, user1.getUserId()));
    }

    /**
     * Tilføj en bruger der er allerede tilføjet
     */
    @Test
    void addExistingUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId()));
        assertEquals(2, room.getMembers().size());
    }

    /**
     * Fjern en bruger fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        model.getRooms().removeMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        assertEquals(room.getMembers().size(), 1);
    }

    /**
     * Fjern en bruger til et rum, der ikke findes
     */
    @org.junit.jupiter.api.Test
    void removeUserToNonExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(69, user2.getUserId(), user1.getUserId()));
    }

    /**
     * Fjern en bruger til et rum, men som en bruger der ikke er admin i rummet
     */
    @org.junit.jupiter.api.Test
    void removeUserNonAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        var user3 = model.getProfiles().createProfile("jørn123", "12345678");
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), user2.getUserId(), user3.getUserId()));

        assertEquals(room.getMembers().size(), 2);
    }

    /**
     * Fjern en bruger fra et rum som bruger som ikke findes
     */
    @Test
    void removeNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), 123, user1.getUserId()));

    }

    /**
     * Fjern en administrator fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeAdminUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), user1.getUserId(), user2.getUserId()));

        assertEquals(room.getMembers().size(), 2);
    }

    /**
     * Fjern en bruger fra et rum, som ikke deltager af rummet
     */
    @Test
    void removeUserWithoutBeingInRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), user2.getUserId(), user3.getUserId()));
        assertEquals(1, room.getMembers().size());
    }

    /**
     * Fjern en bruger der ikke findes til et rum
     */
    @Test
    void removeNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), 123, user1.getUserId()));
    }

    /**
     * Fjern en bruger som ikke er deltager i rummet
     */
    @Test
    void removeNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().removeMember(room.getRoomId(), user3.getUserId(), user1.getUserId()));
        assertEquals(1, room.getMembers().size());
    }

    /**
     * Fjern sig selv fra et rum
     */
    @org.junit.jupiter.api.Test
    void removeSelf() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());

        model.getRooms().removeMember(room.getRoomId(), user2.getUserId(), user2.getUserId());

        assertEquals(room.getMembers().size(), 1);
    }

    /**
     * Ændre navn på chatrum
     */
    @org.junit.jupiter.api.Test
    void changeRoomName() {
        model.getRooms().setName(room.getRoomId(), "test2", user1.getUserId());

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test2");
    }

    /**
     * Ændre navn på chatrum, ikke admin
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameNonAdmin() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "test2", user2.getUserId()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test");
    }

    /**
     * Ændre navn til null
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToNull() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), null, user1.getUserId()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test");
    }

    /**
     * Ændre navn til ""
     */
    @org.junit.jupiter.api.Test
    void changeRoomNameToEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setName(room.getRoomId(), "", user1.getUserId()));

        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test");
    }

    /**
     * Ændre navn, som ikke deltager af rummet
     */
    @Test
    void changeRoomNameNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "GRR", user3.getUserId()));
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test");
    }

    /**
     * Ændre navn, som ikke eksisterende bruger
     */
    @Test
    void changeRoomNameNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(room.getRoomId(), "GRR", 123224));
        assertEquals(model.getRooms().getRoom(room.getRoomId(), user1.getUserId()).getName(), "test");
    }

    /**
     * Ændre navn på et rum der ikke findes
     */
    @org.junit.jupiter.api.Test
    void changeNonExistingRoomName() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setName(123, "", user1.getUserId()));
    }

    /**
     * Muter en bruger i et chatrum
     */
    @Test
    void muteUser() {
        room.addMember(user2.getUserId(), user1.getUserId());
        room.muteUser(user2.getUserId(), user1.getUserId());

        // Skal throw, fordi user2 skal ikke kunne skrive
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUserId()));
    }

    /**
     * Unmuter en muted bruger i et chatrum
     */
    @Test
    void unmuteUser() {
        room.addMember(user2.getUserId(), user1.getUserId());
        // mute først
        room.muteUser(user2.getUserId(), user1.getUserId());
        // unmute igen
        room.unmuteUser(user2.getUserId(), user1.getUserId());

        // Skal ikke kaste
        assertDoesNotThrow(() -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUserId()));
    }

    /**
     * Muter en muted bruger
     */
    @Test
    void muteMutedUser() {
        room.addMember(user2.getUserId(), user1.getUserId());
        room.muteUser(user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * Unmuter en unmuted bruger
     */
    @Test
    void unmuteUnmutedUser() {
        room.addMember(user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * En bruger muter dem selv
     */
    @Test
    void muteThemself() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUserId(), user1.getUserId()));
    }

    /**
     * Muter en bruger uden tilladelse til at mute
     */
    @Test
    void muteWithoutPermission() {
        room.addMember(user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUserId(), user2.getUserId()));
    }

    /**
     * Unmuter en bruger uden tilladelse
     */
    @Test
    void unmuteWithoutPermission() {
        room.addMember(user2.getUserId(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user1.getUserId(), user2.getUserId()));
    }

    /**
     * Muter en bruger der ikke er i chatrummet
     */
    @Test
    void muteUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * Unmuter en bruger der ikke er i chatrummet
     */
    @Test
    void unmuteUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * Mute en bruger som er admin
     */
    @Test
    void muteAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.muteUser(user1.getUserId(), user2.getUserId()));
    }

    /**
     * Unmute en bruger som er admin
     */
    @Test
    void unmuteAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user1.getUserId(), user2.getUserId()));
    }

    /**
     * Mute en bruger ikke deltager i rummet
     */
    @Test
    void muteNotMemberUser() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * Unmute en bruger ikke deltager i rummet
     */
    @Test
    void unmuteNotMemberUser() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUserId(), user1.getUserId()));
    }

    /**
     * Mute en bruger som en bruger der ikke findes
     */
    @Test
    void muteMemberWithoutBeingExisting() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.muteUser(user2.getUserId(), 1234));
    }

    /**
     * Unmute en bruger som en bruger der ikke findes
     */
    @Test
    void unmuteMemberWithoutBeingExisting() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(user2.getUserId(), 1234));
    }

    /**
     * Mute en bruger der ikke findes
     */
    @Test
    void muteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.muteUser(1234, user1.getUserId()));
    }

    /**
     * Unmute en bruger der ikke findes
     */
    @Test
    void unmuteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.unmuteUser(1234, user1.getUserId()));
    }

    /**
     * Forfremmer en bruger der ikke findes
     */
    @Test
    void promoteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.promoteUser(1234, user1.getUserId()));
    }

    /**
     * Degradere en bruger der ikke findes
     */
    @Test
    void demoteNotExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.demoteUser(1234, user1.getUserId()));
    }

    /**
     * Forfrommer en bruger, som en bruger der ikke findes.
     */
    @Test
    void promoteByNotExistingUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.promoteUser(user2.getUserId(), 1234));
        assertEquals(RegularState.class, room.getMember(user2.getUserId()).getState().getClass());
    }

    /**
     * Degradere en bruger, som en bruger der ikke findes.
     */
    @Test
    void demoteByNotExistingUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.demoteUser(user2.getUserId(), 1234));
        assertEquals(RegularState.class, room.getMember(user2.getUserId()).getState().getClass());
    }

    /**
     * Froremmer en bruger der ikke findes i rum
     */
    @Test
    void promoteNotExistingMember() {
        assertThrows(IllegalStateException.class, () -> room.promoteUser(user3.getUserId(), user1.getUserId()));
    }

    /**
     * Degradere en bruger der ikke findes i rum
     */
    @Test
    void demoteNotExistingMember() {
        assertThrows(IllegalStateException.class, () -> room.demoteUser(user3.getUserId(), user1.getUserId()));
    }

    /**
     * Forfremmer en bruger i rummet
     */
    @Test
    void promoteUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        room.promoteUser(user2.getUserId(), user1.getUserId());
        assertEquals(AdministratorState.class, room.getMember(user2.getUserId()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet
     */
    @Test
    void demoteUser() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        room.promoteUser(user2.getUserId(), user1.getUserId());
        room.demoteUser(user2.getUserId(), user1.getUserId());
        assertEquals(RegularState.class, room.getMember(user2.getUserId()).getState().getClass());
    }

    /**
     * Forfremmer en admin
     */
    @Test
    void promoteAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        room.promoteUser(user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.promoteUser(user1.getUserId(), user2.getUserId()));
    }

    /**
     * Degradere en admin
     */
    @Test
    void demoteAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        room.promoteUser(user2.getUserId(), user1.getUserId());
        room.demoteUser(user1.getUserId(), user2.getUserId());
        assertEquals(RegularState.class, room.getMember(user1.getUserId()).getState().getClass());
    }

    /**
     * Forfremmer en bruger i rummet, som ikke-deltager af rummet
     */
    @Test
    void promoteUserWithoutBeingMember() {
        assertThrows(IllegalStateException.class, () -> room.promoteUser(user1.getUserId(), user3.getUserId()));
        assertEquals(AdministratorState.class, room.getMember(user1.getUserId()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet, som ikke-deltager af rummet
     */
    @Test
    void demoteUserWithoutBeingMember() {
        assertThrows(IllegalStateException.class, () -> room.demoteUser(user1.getUserId(), user3.getUserId()));
        assertEquals(AdministratorState.class, room.getMember(user1.getUserId()).getState().getClass());
    }

    /**
     * Forfremmer en bruger i rummet, som ikke-admin af rummet
     */
    @Test
    void promoteUserWithoutBeingAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.promoteUser(user1.getUserId(), user2.getUserId()));
        assertEquals(AdministratorState.class, room.getMember(user1.getUserId()).getState().getClass());
    }

    /**
     * Degradere en bruger i rummet, som ikke-admin af rummet
     */
    @Test
    void demoteUserWithoutBeingAdmin() {
        model.getRooms().addMember(room.getRoomId(), user2.getUserId(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> room.demoteUser(user1.getUserId(), user2.getUserId()));
        assertEquals(AdministratorState.class, room.getMember(user1.getUserId()).getState().getClass());
    }

    @Test
    void changeNicknameOnUser() {
        room.setNicknameOfUser(user1.getUserId(), "test");
        assertEquals("test", room.getMember(user1.getUserId()).getNickname());
    }

    @Test
    void changeNicknameOnNonExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.setNicknameOfUser(12345, "test"));
    }

    @Test
    void changeNicknameOnUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.setNicknameOfUser(user3.getUserId(), "test"));
    }

    @Test
    void setNicknameToNull() {
        assertThrows(IllegalArgumentException.class, () -> room.setNicknameOfUser(user1.getUserId(), null));
    }

    @Test
    void setNicknameToEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> room.setNicknameOfUser(user1.getUserId(), ""));
    }

    @Test
    void deleteNicknameFromUser() {
        room.removeNicknameFromUser(user1.getUserId());
        assertNull(room.getMember(user1.getUserId()).getNickname());
    }

    @Test
    void deleteNicknameFromNonExistingUser() {
        assertThrows(IllegalStateException.class, () -> room.removeNicknameFromUser(12345));
    }

    @Test
    void deleteNicknameFromUserNotInRoom() {
        assertThrows(IllegalStateException.class, () -> room.removeNicknameFromUser(user2.getUserId()));
    }

    /**
     * Skift farve i et rum
     */
    @Test
    void editColor() {
        model.getRooms().setColor(room.getRoomId(), user1.getUserId(), "#1a7ba8");
        assertEquals("#1a7ba8", room.getColor());
    }

    /**
     * Skift farve i et rum der ikke findes
     */
    @Test
    void editColorInNoExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setColor(1234, user1.getUserId(), "#1a7ba8"));
    }

    /**
     * Skift farve i et rum som en bruger der ikke findes
     */
    @Test
    void editColorByNoneExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setColor(room.getRoomId(), 1234, "#1a7ba8"));
        assertNotEquals("#1a7ba8", room.getColor());
    }

    /**
     * Skift farve i et rum, som ikke medlem af et rum
     */
    @Test
    void editColorByNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setColor(room.getRoomId(), user2.getUserId(), "#1a7ba8"));
        assertNotEquals("#1a7ba8", room.getColor());
    }

    /**
     * Skift farve i et rum, til en tom String
     */
    @Test
    void editColorToEmpty() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setColor(room.getRoomId(), user1.getUserId(), ""));
        assertEquals("#ffffff", room.getColor());
    }

    /**
     * Skift farve til null
     */
    @Test
    void editColorToNull() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setColor(room.getRoomId(), user1.getUserId(), null));
        assertEquals("#ffffff", room.getColor());
    }

    /**
     * Skift farve til ikke hex kode
     */
    @Test
    void editColorToNotHexa() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setColor(room.getRoomId(), user1.getUserId(), "#12345g"));
        assertEquals("#ffffff", room.getColor());
    }

    /**
     * Skift skrifttypen i et chatrum
     */
    @Test
    void editFont() {
        model.getRooms().setFont(room.getRoomId(), user1.getUserId(), "Courier New");
        assertEquals("Courier New", room.getFont());
    }

    /**
     * Skift skrifttypen i et rum der ikke findes
     */
    @Test
    void editFontInNoExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setFont(1234, user1.getUserId(), "Courier New"));
    }

    /**
     * Skift skrifttypen i rummet, som en brugere der ikke findes
     */
    @Test
    void editFontByNoExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setFont(room.getRoomId(), 1234, "Courier New"));
        assertEquals("Arial", room.getFont());
    }

    /**
     * Skift skrifttypen i rummet, som en brugere der ikke medlem
     */
    @Test
    void editFontByNotMember() {
        assertThrows(IllegalStateException.class, () -> model.getRooms().setFont(room.getRoomId(), user2.getUserId(), "Courier New"));
        assertEquals("Arial", room.getFont());
    }

    /**
     * Skift skrifttypen i rummet, til en tom String
     */
    @Test
    void editFontToEmpty() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setFont(room.getRoomId(), user1.getUserId(), ""));
        assertEquals("Arial", room.getFont());
    }

    /**
     * Skift skrifttypen i rummet, til Null
     */
    @Test
    void editFontToNull() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setFont(room.getRoomId(), user1.getUserId(), null));
        assertEquals("Arial", room.getFont());
    }

    /**
     * Skift skrifttypen i rummet, til en type der ikke findes
     */
    @Test
    void editFontToNotExistingFont() {
        assertThrows(IllegalArgumentException.class, () -> model.getRooms().setFont(room.getRoomId(), user1.getUserId(), "Sans-serif"));
        assertEquals("Arial", room.getFont());
    }
}
