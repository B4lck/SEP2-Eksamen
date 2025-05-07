package model;

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
    public Message sendMessage(long chatroom, String messageBody, List<String> attachments, long senderId) {
        var message = new ArrayListMessage(senderId, messageBody, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000L, chatroom);

        if (messageBody == null) throw new IllegalStateException("Body cannot be null");
        if (messageBody.isBlank() && !attachments.isEmpty()) throw new IllegalStateException("Body cannot be blank");

        if (senderId == -1)
            throw new IllegalStateException("Du skal være logget ind for at sende en besked i et chatroom");

        if (model.getRooms().getRoom(chatroom,senderId).isMuted(senderId))
            throw new IllegalStateException("Du snakker for meget brormand");

        // Tilføj bilag
        for (String attachment : attachments) {
            if (attachment == null) throw new IllegalStateException("Attachment cannot be null");
            message.addAttachment(attachment);
        }

        messages.add(message);

        // Broadcast besked
        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));

        return message;
    }

    @Override
    public Message sendMessage(long chatroom, String messageBody, long senderID, long time) {
        // Kun til brug til dummy data for nu
        var message = new ArrayListMessage(senderID, messageBody, time, chatroom);
        messages.add(message);
        property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));
        return message;
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
    public List<Message> getMessagesBefore(long messageId, int amount) {
        var beforeMessage = getMessage(messageId);
        return messages.stream()
                .filter(
                        msg -> msg.getChatRoom() == beforeMessage.getChatRoom()
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
    public void editMessage(long messageId, String messageBody, long byUserId) {
        var message = getMessage(messageId);
        message.editBody(messageBody, byUserId);
    }

    @Override
    public void deleteMessage(long messageId, long byUserId) {
        var message = getMessage(messageId);
        message.deleteContent(byUserId);
    }

    @Override
    public void handleRequest(ServerRequest request) {
        long chatRoom;
        var data = request.getData();
        int amount;

        try {
            switch (request.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = data.getLong("chatroom");
                    String messageBody = data.getString("body");

                    List<String> attachments = new ArrayList<>();

                    while (!request.getAttachments().isEmpty()) {
                        var name = request.downloadNextAttachment();
                        System.out.println("tilføjer bilag...");
                        attachments.add(name);
                    }

                    sendMessage(chatRoom, messageBody, attachments, request.getUser());

                    request.respond("Beskeden blev sendt");
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = data.getLong("chatroom");
                    amount = data.getInt("amount");

                    request.respond(new DataMap().with("messages", toSendableData(getMessages(chatRoom, amount))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_BEFORE":
                    long before = request.getData().getLong("before");
                    amount = data.getInt("amount");

                    var messages = getMessagesBefore(before, amount);

                    if (messages.isEmpty()) {
                        request.respond(new DataMap()
                                .with("messages", new ArrayList<DataMap>())
                                .with("newest_time", 0));
                        return;
                    }

                    request.respond(new DataMap()
                            .with("messages", toSendableData(messages))
                            .with("newest_time", messages.getFirst().getDateTime()));
                    break;
                case "EDIT_MESSAGE":
                    long messageId = data.getLong("messageId");
                    String body = data.getString("body");

                    editMessage(messageId, body, request.getUser());

                    request.respond("Beskeden blev ændret");

                    property.firePropertyChange("UPDATE_MESSAGE", null, new DataMap()
                            .with("messageId", messageId)
                            .with("body", body));

                    sendSystemMessage(getMessage(messageId).getChatRoom(), model.getProfiles().getProfile(request.getUser()).getUsername() + " har ændret en besked");
                    break;
                case "DELETE_MESSAGE":
                    messageId = data.getLong("messageId");

                    deleteMessage(messageId, request.getUser());

                    request.respond("Beskeden blev slettet");

                    property.firePropertyChange("UPDATE_MESSAGE", null, new DataMap()
                            .with("messageId", messageId)
                            .with("body", getMessage(messageId).getBody()));

                    sendSystemMessage(getMessage(messageId).getChatRoom(), model.getProfiles().getProfile(request.getUser()).getUsername() + " har slettet en besked");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
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
