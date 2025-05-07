package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesArrayListManagerTest {

    @Test
    void editMessage() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");
        Room room = model.getRooms().createRoom("testrum", user.getUUID());

        // opret besked i chatrummet af user
        String oldMessage = "test1";
        Message message = model.getMessages().sendMessage(room.getRoomId(), oldMessage, user.getUUID(), 0);

        // rediger beskeden
        String newMessage = "test2";
        model.getMessages().editMessage(message.getMessageId(), newMessage, user.getUUID());

        // tjek
        assertEquals(newMessage + " (redigeret)", message.getBody());
    }

    @Test
    void deleteMessage() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");
        Room room = model.getRooms().createRoom("test", user.getUUID());

        // opret besked i chatrummet af user
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", user.getUUID(), 0);

        // slet beskeden
        model.getMessages().deleteMessage(message.getMessageId(), user.getUUID());

        assertEquals("[BESKEDEN ER BLEVET SLETTET]", message.getBody());
    }

    @Test
    void editNonExistingMessage() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().editMessage(-1, "ny besked", user.getUUID()));
    }

    @Test
    void deleteNonExistingMessage() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().deleteMessage(-1, user.getUUID()));
    }

    @Test
    void editMessageWithoutPermission() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test", user1.getUUID());

        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", user1.getUUID(), 0);

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().editMessage(message.getMessageId(), "ny besked", user2.getUUID()));
    }

    @Test
    void deleteMessageWithoutPermission() {
        Model model = new ChatModel();
        Profile user1 = model.getProfiles().createProfile("jens123", "123");
        Profile user2 = model.getProfiles().createProfile("karl123", "321");
        Room room = model.getRooms().createRoom("test", user1.getUUID());

        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", user1.getUUID(), 0);

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().deleteMessage(message.getMessageId(), user2.getUUID()));
    }

    @Test
    void editMessageBodyToEmptyString() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");
        Room room = model.getRooms().createRoom("test", user.getUUID());

        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", user.getUUID(), 0);

        // Tjek om den kaster fejl
        assertThrows(IllegalArgumentException.class, () -> model.getMessages().editMessage(message.getMessageId(), "", user.getUUID()));
    }

    @Test
    void editMessageBodyToNull() {
        Model model = new ChatModel();
        Profile user = model.getProfiles().createProfile("jens123", "123");
        Room room = model.getRooms().createRoom("test", user.getUUID());

        // opret besked
        Message message = model.getMessages().sendMessage(room.getRoomId(), "test", user.getUUID(), 0);

        // Tjek om den kaster fejl
        assertThrows(NullPointerException.class, () -> model.getMessages().editMessage(message.getMessageId(), null, user.getUUID()));
    }
}
