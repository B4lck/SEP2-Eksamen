package model;

import java.util.ArrayList;

public interface ChatRooms {
    void sendMessage(long ChatRoomID, String messageBody, long senderID);
    ArrayList<Message> getMessages(long ChatRoomID, int amount);
    ArrayList<Message> getMessagesSince(long ChatRoomID, long timestamp);
}
