package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        room = model.getRooms().createRoom("test", user1.getUserId());
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
        Message message = model.getMessages().sendMessage(room.getRoomId(), oldMessage, new ArrayList<>(), user1.getUserId());

        // rediger beskeden
        String newMessage = "test2";
        model.getMessages().editMessage(message.getMessageId(), newMessage, user1.getUserId());

        // tjek
        assertEquals(newMessage + " (redigeret)", model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getBody());
    }

    /**
     * Sletter en besked
     */
    @Test
    void deleteMessage() {
        // opret besked i chatrummet af user
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        // slet beskeden
        model.getMessages().deleteMessage(message.getMessageId(), user1.getUserId());

        assertEquals("[BESKEDEN ER BLEVET SLETTET]", model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getBody());
    }

    /**
     * Rediger en ikke eksisterende besked
     */
    @Test
    void editNonExistingMessage() {
        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(-1, "ny besked", user1.getUserId()));
    }

    /**
     * Slet ikke eksisterende besked
     */
    @Test
    void deleteNonExistingMessage() {
        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(-1, user1.getUserId()));
    }

    /**
     * Rediger besked uden tilladelse
     */
    @Test
    void editMessageWithoutPermission() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(message.getMessageId(), "ny besked", user2.getUserId()));
    }

    /**
     * Slet besked uden tilladelse
     */
    @Test
    void deleteMessageWithoutPermission() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        // Tjek om den kaster fejl
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(message.getMessageId(), user2.getUserId()));
    }

    @Test
    void editMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> model.getMessages().editMessage(message.getMessageId(), "test2", 123));
    }

    @Test
    void deleteMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());
        assertThrows(IllegalStateException.class, () -> model.getMessages().deleteMessage(message.getMessageId(), 123));
    }

    /**
     * Rediger besked til en tom besked
     */
    @Test
    void editMessageBodyToEmptyString() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().editMessage(message.getMessageId(), "", user1.getUserId()));
    }

    /**
     * Rediger en beskeds body til null
     */
    @Test
    void editMessageBodyToNull() {
        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        // Tjek om den kaster fejl
        assertThrows(NullPointerException.class, () -> model.getMessages().editMessage(message.getMessageId(), null, user1.getUserId()));
    }

    @Test
    void sendNormalMessage() {
        assertDoesNotThrow(() -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId()));
    }

    @Test
    void sendMessageToNonExistentRoom() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(0, "test", new ArrayList<>(), user1.getUserId()));
    }

    @Test
    void sendMessageToRoomUserIsNotIn() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user2.getUserId()));
    }

    @Test
    void sendMessageAsNonExistingUser() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), 1));
    }

    @Test
    void sendMessageWithNullBody() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendMessage(room.getRoomId(), null, new ArrayList<>(), user1.getUserId()));
    }

    @Test
    void sendMessageWithEmptyBodyAndNoAttachments() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().sendMessage(room.getRoomId(), "", new ArrayList<>(), user1.getUserId()));
    }

    @Test
    void sendMessageWithOneAttachment() {
        ArrayList<String> attachments = new ArrayList<>();
        attachments.add("test");

        Message message = model.getMessages().sendMessage(room.getRoomId(), "", attachments, user1.getUserId());
        assertEquals(1, message.getAttachments().size());
    }

    @Test
    void sendMessagesWithMultipleAttachments() {
        ArrayList<String> attachments = new ArrayList<>();
        attachments.add("test1");
        attachments.add("test2");
        attachments.add("test3");

        Message message = model.getMessages().sendMessage(room.getRoomId(), "", attachments, user1.getUserId());
        assertEquals(3, message.getAttachments().size());
    }

    @Test
    void sendMessageWithNullAttachment() {
        assertThrows(NullPointerException.class ,() -> model.getMessages().sendMessage(room.getRoomId(), "", null, user1.getUserId()));
    }

    @Test
    void getMessagesWithTenAmount() {
        assertDoesNotThrow(() -> model.getMessages().getMessages(room.getRoomId(), 10, user1.getUserId()));
    }

    @Test
    void getMessagesWithNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessages(room.getRoomId(), -1, user1.getUserId()));
    }

    @Test
    void getMessagesWith0Amount() {
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessages(room.getRoomId(), 0, user1.getUserId()));
    }

    @Test
    void getMessagesWith1Amount() {
        assertDoesNotThrow(() -> model.getMessages().getMessages(room.getRoomId(), 1, user1.getUserId()));
    }

    @Test
    void getMessagesFromNonExistingRoom() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessages(7238947, 1, user1.getUserId()));
    }

    @Test
    void getMessagesFromRoomWithNoAcces() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessages(room.getRoomId(), 10, user2.getUserId()));
    }

    @Test
    void getMessagesFromRoomWithNonExistingProfile() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessages(room.getRoomId(), 10, 123));
    }

    @Test
    void getMessagesBeforeExistingMessageWithTenAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        assertDoesNotThrow(() -> model.getMessages().getMessagesBefore(message.getMessageId(), 10, user1.getUserId()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithOneAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        assertDoesNotThrow(() -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, user1.getUserId()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithZeroAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(),"test", new ArrayList<>(), user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 0, user1.getUserId()));
    }

    @Test
    void getMessagesBeforeExistingMessageWithNegativeAmount() {
        Message message = model.getMessages().sendMessage(room.getRoomId(),"test", new ArrayList<>(), user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), -1, user1.getUserId()));
    }

    @Test
    void getMessagesBeforeNonExistingMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(123, 10, user1.getUserId()));
    }

    @Test
    void getMessagesBeforeFromRoomWithNoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, user2.getUserId()));
    }

    @Test
    void getMessagesBeforeFromRoomWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessagesBefore(message.getMessageId(), 1, 123));
    }

    @Test
    void getExistingMessage() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());
        assertDoesNotThrow(() -> model.getMessages().getMessage(message.getMessageId(), user1.getUserId()));
    }

    @Test
    void getNonExistingMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessage(7912783, user1.getUserId()));
    }

    @Test
    void getMessageWithNoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().getMessage(message.getMessageId(), user2.getUserId()));
    }

    @Test
    void getMessageWithNonExistingUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", new ArrayList<>(), user1.getUserId());

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

    @Test
    void addReaction_Regular() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());

        assertEquals(1, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void addReaction_Null() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().addReaction(message.getMessageId(), null, user1.getUserId()));

        assertTrue(model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().isEmpty());
    }

    @Test
    void addReaction_Blank() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().addReaction(message.getMessageId(), " ", user1.getUserId()));

        assertTrue(model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().isEmpty());
    }

    @Test
    void addReaction_NoUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().addReaction(message.getMessageId(), "👌", -1));

        assertTrue(model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().isEmpty());
    }

    @Test
    void addReaction_NoMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().addReaction(-1, "👌", user1.getUserId()));
    }

    @Test
    void addReaction_NoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().addReaction(message.getMessageId(), "👌", user2.getUserId()));

        assertTrue(model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().isEmpty());
    }

    @Test
    void removeReaction_Regular() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "😒", user1.getUserId());
        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());
        model.getMessages().removeReaction(message.getMessageId(), "👌", user1.getUserId());

        assertEquals(1, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void removeReaction_Null() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "😒", user1.getUserId());
        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().removeReaction(message.getMessageId(), null, user1.getUserId()));

        assertEquals(2, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void removeReaction_Blank() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "😒", user1.getUserId());
        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());

        assertThrows(IllegalArgumentException.class, () -> model.getMessages().removeReaction(message.getMessageId(), " ", user1.getUserId()));

        assertEquals(2, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void removeReaction_NoUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "😒", user1.getUserId());
        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().removeReaction(message.getMessageId(), "👌", -1));

        assertEquals(2, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void removeReaction_NoMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().removeReaction(-1, "👌", user1.getUserId()));
    }

    @Test
    void removeReaction_NoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().addReaction(message.getMessageId(), "😒", user1.getUserId());
        model.getMessages().addReaction(message.getMessageId(), "👌", user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().removeReaction(message.getMessageId(), "👌", user2.getUserId()));

        assertEquals(2, model.getMessages().getMessage(message.getMessageId(), user1.getUserId()).getReactions().size());
    }

    @Test
    void setLatestReadMessage_Regular() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        model.getMessages().setLatestReadMessage(message.getMessageId(), user1.getUserId());

        assertEquals(message.getMessageId(), room.getProfile(user1.getUserId()).getLatestReadMessage());
    }

    @Test
    void setLatestReadMessage_NoUser() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().setLatestReadMessage(message.getMessageId(), -1));
    }

    @Test
    void setLatestReadMessage_NoMessage() {
        assertThrows(IllegalStateException.class, () -> model.getMessages().setLatestReadMessage(-1, user1.getUserId()));
    }

    @Test
    void setLatestReadMessage_NoAccess() {
        Message message = model.getMessages().sendMessage(room.getRoomId(), "Test", List.of(), user1.getUserId());

        assertThrows(IllegalStateException.class, () -> model.getMessages().setLatestReadMessage(message.getMessageId(), user2.getUserId()));
    }

}
