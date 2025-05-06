package model;

import mediator.ClientMessage;
import mediator.ServerRequest;
import utils.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessagesArrayListManager implements Messages {
    private ArrayList<Message> messages = new ArrayList<>();

    private PropertyChangeSupport property = new PropertyChangeSupport(this);
    private Model model;

    public MessagesArrayListManager(Model model) {
        this.model = model;
    }

    @Override
    public void sendMessage(long chatroom, String messageBody, long senderId) {
        var message = new ArrayListMessage(senderId, messageBody, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L, chatroom);

        if (senderId == -1)
            throw new IllegalStateException("Du skal være logget ind for at sende en besked i et chatroom");

        // Thrower hvis rummet ikke findes, eller hvis brugerne ikke har adgang til rummet.
        model.getRooms().getRoom(chatroom, senderId);

        messages.add(message);

        // Broadcast besked
        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));
    }

    @Override
    public void sendMessage(long chatroom, String messageBody, long senderID, long time) {
        // Kun til brug til dummy data for nu
        var message = new ArrayListMessage(senderID, messageBody, time, chatroom);
        messages.add(message);
        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));
    }

    @Override
    public List<Message> getMessages(long chatroom, int amount) {
        return messages.stream()
                .filter(msg -> msg.getChatRoom() == chatroom)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList();
    }

    @Override
    public List<Message> getMessagesBefore(long chatroom, long messageId, int amount) {
        var beforeMessage = getMessage(messageId);
        return messages.stream()
                .filter(
                        msg -> msg.getChatRoom() == chatroom
                                && msg.getDateTime() <= beforeMessage.getDateTime()
                                && msg.getMessageId() != messageId)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList();
    }

    @Override
    public Message getMessage(long messageId) {
        return messages.stream().filter(msg -> msg.getMessageId() == messageId).findAny().orElseThrow(() -> new IllegalArgumentException("Beskeden findes ikke."));
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
        int amount;

        try {
            switch (message.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = request.getLong("chatroom");
                    String messageBody = request.getString("body");

                    sendMessage(chatRoom, messageBody, message.getUser());

                    message.downloadNextAttachment();

                    message.respond(new ClientMessage("SUCCESS", new DataMap()
                            .with("status", "Message sent")));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = request.getLong("chatroom");
                    amount = request.getInt("amount");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                            .with("messages", toSendableData(getMessages(chatRoom, amount)))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_BEFORE":
                    chatRoom = request.getLong("chatroom");
                    long before = message.getData().getLong("before");
                    amount = request.getInt("amount");

                    var messages = getMessagesBefore(chatRoom, before, amount);

                    if (messages.isEmpty()) {
                        message.respond(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                                .with("messages", new ArrayList<DataMap>())
                                .with("newest_time", 0)));
                        return;
                    }

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                            .with("messages", toSendableData(messages))
                            .with("newest_time", messages.getFirst().getDateTime())));
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
