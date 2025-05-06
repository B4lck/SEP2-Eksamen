package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Messages extends ClientMessageHandler, PropertyChangeSubject {
    /**
     * Send besked til chatrum
     * @param chatroom Chatrummets id
     * @param messageBody Beskedens body
     * @param senderID Id'et på sender
     */
    void sendMessage(long chatroom, String messageBody, long senderID);

    void sendMessage(long chatroom, String messageBody, long senderID, long time);

    /**
     * Henter alle beskeder i et chatrum
     * @param chatroom Chatrummets id
     * @param amount Antal beskeder
     * @return Returnere antal beskeder ud fra amount
     */
    List<Message> getMessages(long chatroom, int amount);

    List<Message> getMessagesBefore(long chatroom, long messageId, int amount);

    Message getMessage(long messageId);

    void sendSystemMessage(long chatroom, String message);

    void editMessage(long messageId, String messageBody, long byUserId);

    void deleteMessage(long messageId, long byUserId);
}
