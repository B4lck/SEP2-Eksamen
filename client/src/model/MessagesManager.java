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

    public ArrayList<Message> getMessages(long chatroom, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                .with("chatroom", chatroom)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");
        var newMessages = new ArrayList<Message>();

        for (var message : reply.getData().getMapArray("messages")) {
            var msg = Message.fromData(message);
            // Undgå duplikeringer
            if (messages.stream().filter(m -> m.getMessageId() == msg.getMessageId()).findAny().isEmpty()) {
                messages.add(msg);
                newMessages.add(msg);
                property.firePropertyChange("NEW_MESSAGE", null, msg);
            }
        }

        property.firePropertyChange("MESSAGES", null, messages);

        return newMessages;
    }

    public ArrayList<Message> getMessagesSince(long chatroom, long since) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_SINCE", new DataMap()
                .with("chatroom", chatroom)
                .with("since", since)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

        var newMessages = new ArrayList<Message>();

        for (var message : reply.getData().getMapArray("messages")) {
            messages.add(Message.fromData(message));
            newMessages.add(Message.fromData(message));
            property.firePropertyChange("NEW_MESSAGE", null, message);
        }

        property.firePropertyChange("MESSAGES", null, messages);

        return newMessages;
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
            property.firePropertyChange("MESSAGES", null, messages);
            property.firePropertyChange("NEW_MESSAGE", null, castedMessage);
        }
    }
}
