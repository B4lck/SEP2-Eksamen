package model;

import mediator.ClientMessage;
import mediator.ServerMessage;
import util.PropertyChangeSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Map;

public class ChatRoomsArrayListManager implements ChatRooms {
    private ArrayList<Message> messages = new ArrayList<>();

    private PropertyChangeSupport property = new PropertyChangeSupport(this);

    @Override
    public void sendMessage(long ChatRoomID, String messageBody, long senderID) {
        var message = new ArrayListMessage(senderID, messageBody, System.currentTimeMillis());

        messages.add(message);

        property.firePropertyChange("RECEIVE_MESSAGE", null, Map.of("message", message));
    }

    @Override
    public ArrayList<Message> getMessages(long ChatRoomID, int amount) {
        ArrayList<Message> list = new ArrayList<>();

        for (int i = 0; i < Math.min(amount, messages.size()); i++) {
            list.add(messages.get(messages.size() - 1 - i));
        }

        return list;
    }

    @Override
    public ArrayList<Message> getMessagesSince(long ChatRoomID, long timestamp) {
        var messages = new ArrayList<Message>();

        for (Message message : this.messages) {
            if (message.getDateTime() >= timestamp) {
                messages.add(message);
            }
        }

        return messages;
    }

    @Override
    public void handleMessage(ServerMessage message) {
        long chatRoom;

        try {
            switch (message.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = ((Double) message.getData().get("chatroom")).longValue();
                    String messageBody = (String) message.getData().get("body");

                    sendMessage(chatRoom, messageBody, message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of("status", "Message sent")));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = ((Double) message.getData().get("chatroom")).longValue();
                    int amount = ((Double) message.getData().get("amount")).intValue();

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", getMessages(chatRoom, amount).toArray())));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_SINCE":
                    chatRoom = ((Double) message.getData().get("chatroom")).longValue();
                    long since = (int) message.getData().get("since");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", getMessagesSince(chatRoom, since).toArray())));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }
}
