package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.PropertyChangeSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRoomManager implements PropertyChangeSubject, BroadcastHandler{
    private ArrayList<Message> messages;
    private PropertyChangeSupport property;
    private ChatClient chatClient = ChatClient.getInstance();

    public ChatRoomManager() {
        this.messages = new ArrayList<>();
        property = new PropertyChangeSupport(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    @Override
    public void receiveBroadcast(ClientMessage message) {
        if (message.getType().equals("RECEIVE_MESSAGE")) {
            Message castedMessage = (Message) message.getData().get("message");
            messages.add(castedMessage);
            property.firePropertyChange("MESSAGES", null, messages);
        }
    }

    public ArrayList<Message> getMessages(long chatroom, int amount) {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES", Map.of("chatroom", chatroom, "amount", amount)));

        try {
            var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

            property.firePropertyChange("MESSAGES", null, messages);

            return new ArrayList<>(List.of((Message[]) reply.getData().get("messages")));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<Message> getMessagesSince(long chatroom, long since) {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_SINCE", Map.of("chatroom", chatroom, "since", since)));

        try {
            var reply = chatClient.waitingForReply("RECEIVE_MESSAGES");

            property.firePropertyChange("MESSAGES", null, messages);

            return new ArrayList<>(List.of((Message[]) reply.getData().get("messages")));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void sendMessage(long chatroom, String body) {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("SEND_MESSAGE", Map.of("chatroom", chatroom, "body", body)));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
