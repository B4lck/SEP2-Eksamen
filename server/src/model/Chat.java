package model;

import util.PropertyChangeSubject;

import java.util.ArrayList;

public interface Chat extends ClientMessageHandler, PropertyChangeSubject {
    /**
     * Send besked til chatrum
     * @param ChatRoomID Chatrummets id
     * @param messageBody Beskedens body
     * @param senderID Id'et på sender
     */
    void sendMessage(long ChatRoomID, String messageBody, long senderID);

    /**
     * Henter alle beskeder i et chatrum
     * @param ChatRoomID Chatrummets id
     * @param amount Antal beskeder
     * @return Returnere antal beskeder ud fra amount
     */
    ArrayList<Message> getMessages(long ChatRoomID, int amount);

    /**
     * Hent alle beskeder siden givet tidspunkt
     * @param ChatRoomID Chatrummets id
     * @param timestamp Tid i unix time
     * @return Returnere alle beskeder siden givet tidspunkt
     */
    ArrayList<Message> getMessagesSince(long ChatRoomID, long timestamp);
}
