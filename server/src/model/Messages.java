package model;

import utils.PropertyChangeSubject;

import java.util.List;

public interface Messages extends ServerRequestHandler, PropertyChangeSubject {
    /**
     * Send besked til chatrum
     *
     * @param chatroom    Chatrummets id
     * @param messageBody Beskedens body
     * @param senderId    Id'et på sender
     * @return Message objekt for beskeden som er blevet sendt
     */
    Message sendMessage(long chatroom, String messageBody, List<String> attachments, long senderId);

    /**
     * Henter alle beskeder i et chatrum
     *
     * @param chatroom Chatrummets id
     * @param amount   Antal beskeder
     * @param userId   Id'et på brugeren som henter beskederne
     * @return Returnere antal beskeder ud fra amount
     */
    List<Message> getMessages(long chatroom, int amount, long userId);

    /**
     * Henter alle beskeder x antal beskeder før en givet besked
     *
     * @param messageId id'et på beskeden
     * @param amount    antal beskeder
     * @param userId    id'et på brugeren som henter beskederne
     */
    List<Message> getMessagesBefore(long messageId, int amount, long userId);

    /**
     * Henter en besked objekt, ud fra et id
     *
     * @param messageId id'et på beskeden
     * @param userId    id'et på brugeren som henter beskeden
     * @return beskeden som objekt
     */
    Message getMessage(long messageId, long userId);

    /**
     * Sender en system besked til et chatrum
     *
     * @param chatroom id'et på chatrummet
     * @param message  beskeden
     */
    void sendSystemMessage(long chatroom, String message);

    /**
     * Redigere en besked
     *
     * @param messageId   id'et på beskeden
     * @param messageBody den nye body
     * @param byUserId    id'et på brugeren, som forsøger at ændre beskeden
     */
    void editMessage(long messageId, String messageBody, long byUserId);

    /**
     * Sletter en besked
     *
     * @param messageId id'et på beskeden
     * @param byUserId  id'et på brugeren, som forsøger at slette beskeden
     */
    void deleteMessage(long messageId, long byUserId);
}
