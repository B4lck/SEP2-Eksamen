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
    // Lokal cache over kendte beskeder
    private final ArrayList<Message> messages;

    private final PropertyChangeSupport property;
    private final ChatClient chatClient = ChatClient.getInstance();

    public MessagesManager() {
        this.messages = new ArrayList<>();
        property = new PropertyChangeSupport(this);
        chatClient.addListener(this); // Aflyt efter broadcasts
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    /**
     * Håndter broadcasts fra serveren.
     */
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
                property.firePropertyChange("NEW_REACTION", null,
                        new Reaction(request.getLong("reactedBy"), request.getString("reaction")));
                break;
        }
    }

    /**
     * Hent beskeder i et rum. Henter fra serveren hvis klienten ikke har cached nok beskeder.
     *
     * @param roomId ID'et på rummet.
     * @param amount Mængden af beskeder som skal hentes.
     * @return En liste af besked objekter.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public List<Message> getMessages(long roomId, int amount) throws ServerError {
        // Hent fra cache hvis det er muligt.
        var cached = messages.stream()
                .filter(m -> m.getRoomId() == roomId)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList().reversed();
        if (cached.size() == amount) return cached;

        // Der er ikke nok beskeder i cache, hent fra serveren.
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES", new DataMap()
                .with("roomId", roomId)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("MessagesManager getMessages");

        // Tilføj de nye beskeder fra serveren.
        addNewMessages(reply.getData().getMapArray("messages"));

        // Og svar igen med beskederne i cachen
        return messages.stream()
                .filter(m -> m.getRoomId() == roomId)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList().reversed();
    }

    /**
     * Hent beskeder fra før den en besked. Spørger altid serveren.
     *
     * @param roomId    ID'et på rummet.
     * @param messageId ID'et på beskeden, hvorfra ældre beskeder skal hentes.
     * @param amount    Mængden af beskeder som skal hentes.
     * @return En liste af besked objekter.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public List<Message> getMessagesBefore(long roomId, long messageId, int amount) throws ServerError {
        chatClient.sendMessage(new ClientMessage("RECEIVE_MESSAGES_BEFORE", new DataMap()
                .with("before", messageId)
                .with("amount", amount)));

        var reply = chatClient.waitingForReply("MessagesManager getMessagesBefore");

        addNewMessages(reply.getData().getMapArray("messages"));

        return messages.stream()
                .filter(m -> m.getRoomId() == roomId
                        && m.getDateTime() <= reply.getData().getLong("newestTime")
                        && m.getMessageId() != messageId)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(amount)
                .toList().reversed();
    }

    /**
     * Tilføj nye beskeder til cachen, udskifter beskeden hvis den allerede findes.
     *
     * @param newMessages En liste af nye beskeder's datamaps.
     */
    private void addNewMessages(List<DataMap> newMessages) {
        for (var message : newMessages) {
            addMessage(Message.fromData(message));
        }
    }

    /**
     * Tilføj en ny besked til cachen, udskifter beskeden hvis den allerede findes.
     *
     * @param message Beskeden som skal tilføjes
     */
    private void addMessage(Message message) {
        messages.removeIf(m -> m.getMessageId() == message.getMessageId());
        messages.add(message);
    }

    /**
     * Sender en besked i et chatrum.
     *
     * @param roomId      ID'et på rummet.
     * @param body        Beskedens body.
     * @param attachments Eventuelle bilag til beskeden.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void sendMessage(long roomId, String body, List<Attachment> attachments) throws ServerError {
        if (attachments.isEmpty()) {
            chatClient.sendMessage(new ClientMessage("SEND_MESSAGE", new DataMap()
                    .with("roomId", roomId)
                    .with("body", body)));
        } else {
            chatClient.sendMessageWithAttachments(new ClientMessage("SEND_MESSAGE", new DataMap()
                    .with("roomId", roomId)
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

    /**
     * Tilføjer en reaktion til en besked.
     *
     * @param messageId Beskedens ID.
     * @param reaction Reaktionen som streng.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void addReaction(long messageId, String reaction) throws ServerError {
        chatClient.sendMessage(new ClientMessage("ADD_REACTION", new DataMap()
                .with("messageId", messageId)
                .with("reaction", reaction)));

        chatClient.waitingForReply("Add reaction");
    }

    /**
     * Fjerner en reaktion fra en besked.
     *
     * @param messageId Beskedens ID.
     * @param reaction Reaktionen som streng.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void removeReaction(long messageId, String reaction) throws ServerError {
        chatClient.sendMessage(new ClientMessage("REMOVE_REACTION", new DataMap()
                .with("messageId", messageId)
                .with("reaction", reaction)));

        chatClient.waitingForReply("Remove reaction");
    }

    /**
     * Marker en besked som læst.
     *
     * @param messageId Beskedens ID.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void readMessage(long messageId) throws ServerError {
        chatClient.sendMessage(new ClientMessage("READ_MESSAGE", new DataMap().with("messageId", messageId)));

        chatClient.waitingForReply("Read message");
    }
}
