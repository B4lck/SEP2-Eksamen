package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatArrayListManager implements Chat {
    private ArrayList<Message> messages = new ArrayList<>();

    private PropertyChangeSupport property = new PropertyChangeSupport(this);
    private Model model;

    public ChatArrayListManager(Model model) {
        this.model = model;
    }

    @Override
    public void sendMessage(long chatRoomId, String messageBody, long senderId) {
        var message = new ArrayListMessage(senderId, messageBody, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L, chatRoomId);

        if (senderId == -1) throw new IllegalStateException("Du skal være logget ind for at sende en besked i et chatroom");

        messages.add(message);

        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));
    }

    @Override
    public ArrayList<Message> getMessages(long chatRoomId, int amount) {
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
    public void sendSystemMessage(long chatroom, String body) {
        var message = new ArrayListMessage(0, body, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L, chatroom);

        messages.add(message);

        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));
    }

    @Override
    public void handleMessage(ServerRequest message) {
        long chatRoom;
        var request = message.getData();

        try {
            switch (message.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = request.getLong("chatroom");
                    String messageBody = request.getString("body");

                    sendMessage(chatRoom, messageBody, message.getUser());
                    message.respond(new ClientMessage("SUCCESS", new DataMap()
                            .with("status", "Message sent")));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = request.getLong("chatroom");
                    int amount = request.getInt("amount");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                            .with("messages", toSendableData(getMessages(chatRoom, amount)))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_SINCE":
                    chatRoom = request.getLong("chatroom");
                    long since = message.getData().getInt("since");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                            .with("messages", toSendableData(getMessagesSince(chatRoom, since)))));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }

    private List<DataMap> toSendableData(List<Message> messages) {
        return messages.stream().map(Message::getData).toList();
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
