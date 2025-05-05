package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;
import utils.PropertyChangeSubject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessagesManager implements PropertyChangeSubject, PropertyChangeListener {
    private ArrayList<Message> messages;
    private PropertyChangeSupport property;
    private ChatClient chatClient = ChatClient.getInstance();

    public MessagesManager() {
        this.messages = new ArrayList<>();
        property = new PropertyChangeSupport(this);

        chatClient.addListener(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    public List<Message> getMessages(long chatroom, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                .with("chatroom", chatroom)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

        addNewMessages(reply.getData().getMapArray("messages"));

        return messages.stream()
                .filter(m -> m.getChatRoom() == chatroom)
                .sorted(Comparator.comparingLong(Message::getDateTime))
                .limit(amount)
                .toList();
    }

    public List<Message> getMessagesBefore(long chatroom, long messageId, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_BEFORE", new DataMap()
                .with("chatroom", chatroom)
                .with("before", messageId)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

        addNewMessages(reply.getData().getMapArray("messages"));

        return messages.stream()
                .filter(m -> m.getChatRoom() == chatroom
                        && m.getDateTime() <= reply.getData().getLong("newest_time")
                        && m.getMessageId() != messageId)
                .sorted(Comparator.comparingLong(Message::getDateTime))
                .limit(amount)
                .toList();
    }

    private void addNewMessages(List<DataMap> newMessages) {
        for (var message : newMessages) {
            var msg = Message.fromData(message);
            // Undgå duplikeringer
            if (messages.stream().filter(m -> m.getMessageId() == msg.getMessageId()).findAny().isEmpty()) {
                messages.add(msg);
            }
        }
    }

    public void sendMessage(long chatroom, String body) throws ServerError {
        chatClient.sendMessage(new ClientMessage("SEND_MESSAGE", new DataMap()
                .with("chatroom", chatroom)
                .with("body", body)));
        chatClient.waitingForReply("SUCCESS");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var message = (ClientMessage) evt.getNewValue();
        if (message.getType().equals("RECEIVE_MESSAGE")) {
            Message castedMessage = Message.fromData(message.getData().getMap("message"));
            messages.add(castedMessage);
            property.firePropertyChange("NEW_MESSAGE", null, castedMessage);
        }
    }
}
