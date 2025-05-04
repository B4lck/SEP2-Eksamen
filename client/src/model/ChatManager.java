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

public class ChatManager implements PropertyChangeSubject, PropertyChangeListener {
    private ArrayList<ChatMessage> messages;
    private PropertyChangeSupport property;
    private ChatClient chatClient = ChatClient.getInstance();

    public ChatManager() {
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

    public ArrayList<ChatMessage> getMessages(long chatroom, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                .with("chatroom", chatroom)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");
        var newMessages = new ArrayList<ChatMessage>();

        for (var message : reply.getData().getMapArray("messages")) {
            var msg = ChatMessage.fromData(message);
            // Undgå duplikeringer
            if (messages.stream().filter(m -> m.getMessageId() == msg.getMessageId()).findAny().isEmpty()) {
                messages.add(msg);
                newMessages.add(msg);
            }
        }

        property.firePropertyChange("MESSAGES", null, messages);

        return newMessages;
    }

    public ArrayList<ChatMessage> getMessagesSince(long chatroom, long since) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_SINCE", new DataMap()
                .with("chatroom", chatroom)
                .with("since", since)));

        var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

        var newMessages = new ArrayList<ChatMessage>();

        for (var message : reply.getData().getMapArray("messages")) {
            messages.add(ChatMessage.fromData(message));
            newMessages.add(ChatMessage.fromData(message));
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
        System.out.println("modtaget broadcast");
        var message = (ClientMessage) evt.getNewValue();
        if (message.getType().equals("RECEIVE_MESSAGE")) {
            ChatMessage castedMessage = ChatMessage.fromData(message.getData().getMap("message"));
            messages.add(castedMessage);
            property.firePropertyChange("MESSAGES", null, messages);
        }
    }
}
