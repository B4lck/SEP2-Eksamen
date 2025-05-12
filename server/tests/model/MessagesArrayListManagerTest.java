package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MessagesArrayListManagerTest {
    private Model model;
    private Profile user1;
    private Profile user2;
    private Room room;
    
    @BeforeEach
    void init() throws SQLException {
        Database.startTesting();

        model = new ChatModel();

        user1 = model.getProfiles().createProfile("jens123", "123");
        user2 = model.getProfiles().createProfile("karl123", "123");
        room = model.getRooms().createRoom("test", user1.getUUID());
    }

    @AfterEach
    void tearDown() throws SQLException {
        Database.endTesting();
    }

    /**
     * Redigere en besked
     */
    @Test
    void editMessage() {
        // opret besked i chatrummet af user
        String oldMessage = "test1";
        Message message = model.getMessages().sendMessage(room.getRoomId(), oldMessage, new ArrayList<>(), user1.getUUID());

        // rediger beskeden
        String newMessage = "test2";
        model.getMessages().editMessage(message.getMessageId(), newMessage, user1.getUUID());

        // tjek
        assertEquals(newMessage + " (redigeret)", model.getMessages().getMessage(message.getMessageId(), user1.getUUID()).getBody());
    }

    /**
     * Sletter en besked
     */
    @Test
    void deleteMessage() {
        // opret besked i chatrummet af user
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        // slet beskeden
        model.getMessages().deleteMessage(message.getMessageId(), user1.getUUID());

        assertEquals("[BESKEDEN ER BLEVET SLETTET]", model.getMessages().getMessage(message.getMessageId(), user1.getUUID()).getBody());
    }

    /**
     * Rediger en ikke eksisterende besked
     */
    @Test
    void editNonExistingMessage() {
        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(-1, "ny besked", user1.getUUID()));
    }

    /**
     * Slet ikke eksisterende besked
     */
    @Test
    void deleteNonExistingMessage() {
        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(-1, user1.getUUID()));
    }

    /**
     * Rediger besked uden tilladelse
     */
    @Test
    void editMessageWithoutPermission() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(message.getMessageId(), "ny besked", user2.getUUID()));
    }

    /**
     * Slet besked uden tilladelse
     */
    @Test
    void deleteMessageWithoutPermission() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(message.getMessageId(), user2.getUUID()));
    }

    @Test
    void editMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(message.getMessageId(), "test2", 123));
    }

    @Test
    void deleteMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(message.getMessageId(), 123));
    }

    /**
     * Rediger besked til en tom besked
     */
    @Test
    void editMessageBodyToEmptyString() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().editMessage(message.getMessageId(), "", user1.getUUID()));
    }

    /**
     * Rediger en beskeds body til null
     */
    @Test
    void editMessageBodyToNull() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        // Tjek om den kaster fejl
        assertThrows(NullPointerException.class, () -> model.getMessages().editMessage(message.getMessageId(), null, user1.getUUID()));
    }

    @Test
    void sendNormalMessage() {
        assertDoesNotThrow(() -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID()));
    }

    @Test
    void sendMessageToNonExistentRoom() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(0, "test", new ArrayList<>(), user1.getUUID()));
    }

    @Test
    void sendMessageToRoomUserIsNotIn() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUUID()));
    }

    @Test
    void sendMessageAsNonExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), 1));
    }

    @Test
    void sendMessageWithNullBody() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendMessage(room.getRoomId(), null, new ArrayList<>(), user1.getUUID()));
    }

    @Test
    void sendMessageWithEmptyBodyAndNoAttachments() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "", new ArrayList<>(), user1.getUUID()));
    }

    @Test
    void sendMessageWithOneAttachment() {
        ArrayList<String> attachments = new ArrayList<>();
        attachments.add("test");

        Message message = model.getMessages().sendMessage(room.getRoomId(), "", attachments, user1.getUUID());
        assertEquals(1, message.getAttachments().size());
    }

    @Test
    void sendMessagesWithMultipleAttachments() {
        ArrayList<String> attachments = new ArrayList<>();
        attachments.add("test1");
        attachments.add("test2");
        attachments.add("test3");

        Message message = model.getMessages().sendMessage(room.getRoomId(), "", attachments, user1.getUUID());
        assertEquals(3, message.getAttachments().size());
    }

    @Test
    void sendMessageWithNullAttachment() {
        assertThrows(NullPointerException.class ,() -> model.getMessages().sendMessage(room.getRoomId(), "", null, user1.getUUID()));
    }

    @Test
    void getMessagesWithTenAmount() {
        assertDoesNotThrow(() -> model.getMessages().getMessages(room.getRoomId(), 10, user1.getUUID()));
    }

    @Test
    void getMessagesWithNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessages(room.getRoomId(), -1, user1.getUUID()));
    }

    @Test
    void getMessagesWith0Amount() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessages(room.getRoomId(), 0, user1.getUUID()));
    }

    @Test
    void getMessagesWith1Amount() {
        assertDoesNotThrow(() -> model.getMessages().getMessages(room.getRoomId(), 1, user1.getUUID()));
    }

    @Test
    void getMessagesFromNonExistingRoom() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessages(7238947, 1, user1.getUUID()));
    }

    @Test
    void getMessagesFromRoomWithNoAcces() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessages(room.getRoomId(), 10, user2.getUUID()));
    }

    @Test
    void getMessagesFromRoomWithNonExistingProfile() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessages(room.getRoomId(), 10, 123));
    }

    @Test
    void getMessagesBeforeExistingMessageWithTenAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertDoesNotThrow(() -> model.getMessages().getMessagesBefore(message.getMessageId(), 10, user1.getUUID()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithOneAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertDoesNotThrow(() -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, user1.getUUID()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithZeroAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(),"test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 0, user1.getUUID()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithNegativeAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(),"test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), -1, user1.getUUID()));
    }

    @Test
    void getMessagesBeforeNonExistingMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(123, 10, user1.getUUID()));
    }

    @Test
    void getMessagesBeforeFromRoomWithNoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, user2.getUUID()));
    }

    @Test
    void getMessagesBeforeFromRoomWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, 123));
    }

    @Test
    void getExistingMessage() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());
        assertDoesNotThrow(() -> model.getMessages().getMessage(message.getMessageId(), user1.getUUID()));
    }

    @Test
    void getNonExistingMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessage(7912783, user1.getUUID()));
    }

    @Test
    void getMessageWithNoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessage(message.getMessageId(), user2.getUUID()));
    }

    @Test
    void getMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUUID());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessage(message.getMessageId(), 123));
    }

    @Test
    void sendSystemMessageToChatroom() {
        assertDoesNotThrow(() -> model.getMessages().sendSystemMessage(room.getRoomId(), "test"));
    }

    @Test
    void sendSystemMessageToNonExistingChatroom() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendSystemMessage(123, "test"));
    }

    @Test
    void sendNullSystemMessage() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendSystemMessage(room.getRoomId(), null));
    }

    @Test
    void sendEmptySystemMessage() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendSystemMessage(room.getRoomId(), ""));
    }

}
