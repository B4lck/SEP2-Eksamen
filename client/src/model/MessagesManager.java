package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.Attachment;
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

        var reply = chatClient.waitingForReply("MessagesManager getMessages");

        addNewMessages(reply.getData().getMapArray("messages"));

        return messages.stream()
                .filter(m -> m.getChatRoom() == chatroom)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList().reversed();
    }

    public List<Message> getMessagesBefore(long chatroom, long messageId, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_BEFORE", new DataMap()
                .with("before", messageId)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("MessagesManager getMessagesBefore");

        addNewMessages(reply.getData().getMapArray("messages"));

        return messages.stream()
                .filter(m -> m.getChatRoom() == chatroom
                        && m.getDateTime() <= reply.getData().getLong("newest_time")
                        && m.getMessageId() != messageId)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList().reversed();
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

    public void sendMessage(long chatroom, String body, List<Attachment> attachments) throws ServerError {
        if (attachments.isEmpty()) {
            chatClient.sendMessage(new ClientMessage("SEND_MESSAGE", new DataMap()
                    .with("chatroom", chatroom)
                    .with("body", body)));
        } else {
            chatClient.sendMessageWithAttachments(new ClientMessage("SEND_MESSAGE", new DataMap()
                    .with("chatroom", chatroom)
                    .with("body", body)), attachments);
        }
        chatClient.waitingForReply("MessagesManager sendMessage");
    }

    /**
     * Sender besked til server om at redigere en besked
     *
     * @param messageId - ID'et på den besked som skal redigeres
     * @param body      - Den nye body
     * @throws ServerError - Hvis serveren støder på en fejl
     */
    public void editMessage(long messageId, String body) throws ServerError {
        chatClient.sendMessage(new ClientMessage("EDIT_MESSAGE", new DataMap()
                .with("messageId", messageId)
                .with("body", body)));
        chatClient.waitingForReply("SUCCESS");
    }

    /**
     * Sender besked til server om at fjerne en besked
     *
     * @param messageId - ID'et på den besked som skal fjernes
     * @throws ServerError - Hvis serveren støder på en fejl
     */
    public void deleteMessage(long messageId) throws ServerError {
        chatClient.sendMessage(new ClientMessage("DELETE_MESSAGE", new DataMap()
                .with("messageId", messageId)));
        chatClient.waitingForReply("SUCCESS");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var message = (ClientMessage) evt.getNewValue();
        var request = message.getData();

        switch (message.getType().toUpperCase()) {
            case "RECEIVE_MESSAGE":
                Message newMessage = Message.fromData(request.getMap("message"));
                addMessage(newMessage);
                property.firePropertyChange("NEW_MESSAGE", null, newMessage);
                break;
            case "UPDATE_MESSAGE":
                Message updatedMessage = Message.fromData(request.getMap("message"));
                addMessage(updatedMessage);
                property.firePropertyChange("UPDATE_MESSAGE", null, updatedMessage);
                break;
            case "NEW_REACTION":
                property.firePropertyChange("NEW_REACTION", null, new Reaction(request.getLong("reactedBy"), request.getString("reaction")));
                break;
        }
    }

    private void addMessage(Message message) {
        messages.removeIf(m -> m.getMessageId() == message.getMessageId());
        messages.add(message);
    }

    public void addReaction(long messageId, String reaction) throws ServerError {
        chatClient.sendMessage(new ClientMessage("ADD_REACTION", new DataMap()
                .with("messageId", messageId)
                .with("reaction", reaction)));

        chatClient.waitingForReply("Add reaction");
    }

    public void removeReaction(long messageId, String reaction) throws ServerError {
        chatClient.sendMessage(new ClientMessage("REMOVE_REACTION", new DataMap()
                .with("messageId", messageId)
                .with("reaction", reaction)));

        chatClient.waitingForReply("Remove reaction");
    }

    public void readMessage(long messageId) throws ServerError {
        chatClient.sendMessage(new ClientMessage("READ_MESSAGE", new DataMap().with("messageId", messageId)));

        chatClient.waitingForReply("Read message");
    }
}
