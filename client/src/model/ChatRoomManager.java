package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.PropertyChangeSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Map;

public class ChatRoomManager implements PropertyChangeSubject, BroadcastHandler{
    private ArrayList<Message> messages;
    private PropertyChangeSupport property;

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
        }
    }

    public ArrayList<Message> getMessages(long chatroom, int amount) {
        ArrayList<Message> messagesToReturn = new ArrayList<>();
        for (int i = 0; i > amount; i++) {
            try {
                messagesToReturn.add(messages.get(messages.size() - 1 - i));
            } catch (Exception e) {
                break;
            }
        }
        return messagesToReturn;
    }

    public ArrayList<Message> getMessagesSince(long chatroom, long since) {
        ArrayList<Message> messagesToReturn = new ArrayList<>();
        for (Message message : messages) {
            if (message.getDateTime() > since) {
                messagesToReturn.add(message);
            }
        }
        return messagesToReturn;
    }

    public void sendMessage(long chatroom, String body) {
        try {
            ChatClient.getInstance().sendMessage(new ClientMessage("SEND_MESSAGE", Map.of("chatroom", chatroom, "body", body)));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
