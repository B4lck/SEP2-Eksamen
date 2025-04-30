package model;

import mediator.ClientMessage;
import mediator.ServerRequest;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Map;

public class ChatRoomsArrayListManager implements ChatRooms {
    private ArrayList<Message> messages = new ArrayList<>();

    private PropertyChangeSupport property = new PropertyChangeSupport(this);

    @Override
    public void sendMessage(long ChatRoomID, String messageBody, long senderID) {
        var message = new ArrayListMessage(senderID, messageBody, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L);

        if (senderID == -1) throw new IllegalStateException("Du skal være logget ind for at sende en besked i et chatroom");

        messages.add(message);

        property.firePropertyChange("RECEIVE_MESSAGE", null, Map.of("message", message.getData()));
    }

    @Override
    public ArrayList<Message> getMessages(long ChatRoomID, int amount) {
        ArrayList<Message> list = new ArrayList<>();

        amount = Math.min(amount, messages.size());

        for (int i = 0; i < amount; i++) {
            list.add(messages.get(messages.size() - amount + i));
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
    public void handleMessage(ServerRequest message) {
        long chatRoom;

        try {
            switch (message.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = Long.parseLong((String) message.getData().get("chatroom"));
                    String messageBody = (String) message.getData().get("body");

                    sendMessage(chatRoom, messageBody, message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of("status", "Message sent")));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = Long.parseLong((String) message.getData().get("chatroom"));
                    int amount = ((Double) message.getData().get("amount")).intValue();

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", toSendableData(getMessages(chatRoom, amount)))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_SINCE":
                    chatRoom = ((Double) message.getData().get("chatroom")).longValue();
                    long since = (int) message.getData().get("since");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", toSendableData(getMessagesSince(chatRoom, since)))));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }

    private Map<String, Object>[] toSendableData(ArrayList<Message> messages) {
        Map<String, Object>[] sendableData = new Map[messages.size()];

        for (int i = 0; i < messages.size(); i++ ) {
            var message = messages.get(i);
            sendableData[i] = message.getData();
        }

        return sendableData;
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
