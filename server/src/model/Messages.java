package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Messages extends ServerRequestHandler, PropertyChangeSubject {
    /**
     * Send besked til chatrum
     *
     * @param chatroom    Chatrummets id
     * @param messageBody Beskedens body
     * @param senderID    Id'et på sender
     * @return Message objekt for beskeden som er blevet sendt
     */
    Message sendMessage(long chatroom, String messageBody, List<String> attachments, long senderID);

    /**
     * PLACEHOLDER TIL DUMMY DATA, FINGRENE VÆK!
     * @param chatroom id'et på chatrummet
     * @param messageBody bodyen på beskeden
     * @param senderID id'et på senderen
     * @param time tidspunkt i unix tid
     * @return Message objekt for beskeden som er blevet sendt
     */
    Message sendMessage(long chatroom, String messageBody, long senderID, long time);

    /**
     * Henter alle beskeder i et chatrum
     * @param chatroom Chatrummets id
     * @param amount Antal beskeder
     * @return Returnere antal beskeder ud fra amount
     */
    List<Message> getMessages(long chatroom, int amount);

    /**
     * Henter alle beskeder x antal beskeder før en givet besked
     * @param messageId id'et på beskeden
     * @param amount antal beskeder
     * @return
     */
    List<Message> getMessagesBefore(long messageId, int amount);

    /**
     * Henter en besked objekt, ud fra et id
     * @param messageId id'et på beskeden
     * @return beskeden som objekt
     */
    Message getMessage(long messageId);

    /**
     * Sender en system besked til et chatrum
     * @param chatroom id'et på chatrummet
     * @param message beskeden
     */
    void sendSystemMessage(long chatroom, String message);

    /**
     * Redigere en besked
     * @param messageId id'et på beskeden
     * @param messageBody den nye body
     * @param byUserId id'et på brugeren, som forsøger at ændre beskeden
     */
    void editMessage(long messageId, String messageBody, long byUserId);

    /**
     * Sletter en besked
     * @param messageId id'et på beskeden
     * @param byUserId id'et på brugeren, som forsøger at slette beskeden
     */
    void deleteMessage(long messageId, long byUserId);
}
