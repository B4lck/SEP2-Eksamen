package model;

import mediator.ServerRequest;
import utils.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessagesDBManager implements Messages {

    private Model model;
    private PropertyChangeSupport property = new PropertyChangeSupport(this);

    public MessagesDBManager(Model model) {
        this.model = model;
        model.addHandler(this);
    }

    @Override
    public Message sendMessage(long chatroom, String messageBody, List<String> attachments, long senderId) {
        if (senderId == -1)
            throw new IllegalStateException("Du skal være logget ind for at sende en besked i et chatroom");

        if (!model.getRooms().doesRoomExists(chatroom))
            throw new IllegalStateException("Rummet findes ikke");

        if (senderId != 0 && model.getRooms().getRoom(chatroom, senderId).isMuted(senderId))
            throw new IllegalStateException("Du snakker for meget brormand");

        if (messageBody == null)
            throw new IllegalArgumentException("Besked må ikke være null");

        if (messageBody.isEmpty() && attachments.isEmpty())
            throw new IllegalArgumentException("Besked er tom");

        try (Connection connection = Database.getConnection()) {
            var currentTime = System.currentTimeMillis();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO message (body, sent_by_id, room_id, time) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, messageBody);
            statement.setLong(2, senderId);
            statement.setLong(3, chatroom);
            statement.setLong(4, currentTime);
            statement.executeUpdate();
            ResultSet res = statement.getGeneratedKeys();

            if (res.next()) {
                var message = new DBMessage(res.getLong(1), senderId, messageBody, currentTime, chatroom);

                for (String attachment : attachments) {
                    message.addAttachment(attachment);
                }

                property.firePropertyChange("RECEIVE_MESSAGE", null, new DataMap().with("message", message.getData()));

                return message;
            }
            else {
                throw new RuntimeException("Kunne ikke oprette besked i databasen, måske");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> getMessages(long chatroom, int amount, long userId) {
        if (amount <= 0) throw new IllegalArgumentException("Ikke nok beskeder");
        if (!model.getRooms().doesRoomExists(chatroom)) throw new IllegalArgumentException("Rummet findes ikke brormand");

        try (Connection connection = Database.getConnection()) {
            // burde throw hvis brugeren ikke har adgang til rummet
            // TODO: Lav en .hasAccessTo(chatroom, userId)
            model.getRooms().getRoom(chatroom, userId);

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM message WHERE room_id = ? LIMIT ?");
            statement.setLong(1, chatroom);
            statement.setInt(2, amount);
            ResultSet res = statement.executeQuery();

            List<Message> messages = new ArrayList<>();

            while (res.next()) {
                messages.add(new DBMessage(
                        res.getLong("id"),
                        res.getLong("sent_by_id"),
                        res.getString("body"),
                        res.getLong("time"),
                        res.getLong("room_id")
                ));
            }

            return messages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> getMessagesBefore(long messageId, int amount, long userId) {
        if (amount <= 0) throw new IllegalArgumentException("Det er for lidt beskeder brormand");
        if (getMessage(messageId, userId) == null) throw new IllegalStateException("Beskeden findes ikke, eller du har ikke adgang til rummet.");

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("WITH beforeThis (time, room_id) AS (SELECT time, room_id FROM message WHERE id = ?)\n" +
                    "SELECT * FROM message, beforeThis WHERE message.time < beforeThis.time AND message.room_id = beforeThis.room_id ORDER BY message.time DESC LIMIT ?;");
            statement.setLong(1, messageId);
            statement.setInt(2, amount);
            ResultSet res = statement.executeQuery();

            List<Message> messages = new ArrayList<>();

            while (res.next()) {
                messages.add(new DBMessage(
                        res.getLong("id"),
                        res.getLong("sent_by_id"),
                        res.getString("body"),
                        res.getLong("time"),
                        res.getLong("room_id")
                ));
            }

            return messages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message getMessage(long messageId, long userId) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM message WHERE id = ?");
            statement.setLong(1, messageId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                model.getRooms().getRoom(res.getLong("room_id"), userId);

                return new DBMessage(
                        res.getLong("id"),
                        res.getLong("sent_by_id"),
                        res.getString("body"),
                        res.getLong("time"),
                        res.getLong("room_id")
                );
            }
            else {
                throw new IllegalStateException("Kunne ikke finde besked med id " + messageId);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendSystemMessage(long chatroom, String message) {
        sendMessage(chatroom, message, new ArrayList<>(), 0);
    }

    @Override
    public void editMessage(long messageId, String messageBody, long byUserId) {
        // Hent beskeden
        var message = getMessage(messageId, byUserId);

        // Rediger beskeden
        message.editBody(messageBody, byUserId);

        // Broadcast til klienter
        property.firePropertyChange("UPDATE_MESSAGE", null, new DataMap().with("message", message.getData()));

        // Send system besked
        sendSystemMessage(message.getChatRoom(),
                model.getProfiles().getProfile(byUserId).map(Profile::getUsername).orElse("En bruger")
                        + " har ændret en besked.");
    }

    @Override
    public void deleteMessage(long messageId, long byUserId) {
        // Hent beskeden
        var message = getMessage(messageId, byUserId);

        // Gem bilag inden content bliver slettet
        List<String> attachments = message.getAttachments();

        // Slet selve beskeden
        message.deleteContent(byUserId);

        // Slet bilagene på serveren, når beskeden er blevet slettet
        UserFilesManager.getInstance().removeFiles(attachments);

        // Broadcast til klienter
        property.firePropertyChange("UPDATE_MESSAGE", null, new DataMap().with("message", message.getData()));

        // Send system besked
        String username = model.getProfiles().getProfile(byUserId).map(Profile::getUsername).orElse("En bruger");
        sendSystemMessage(message.getChatRoom(), username + " har slettet en besked.");
    }

    @Override
    public void handleRequest(ServerRequest request) {
        long chatRoom;
        var data = request.getData();
        int amount;
        long messageId;

        try {
            switch (request.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = data.getLong("chatroom");
                    String messageBody = data.getString("body");

                    if (messageBody == null) messageBody = "";

                    List<String> attachments = new ArrayList<>();

                    // Hent attachments
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

                    request.respond(new DataMap().with("messages", toSendableData(getMessages(chatRoom, amount, request.getUser()))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_BEFORE":
                    long before = request.getData().getLong("before");
                    amount = data.getInt("amount");

                    var messages = getMessagesBefore(before, amount, request.getUser());

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
                    messageId = data.getLong("messageId");
                    String body = data.getString("body");

                    editMessage(messageId, body, request.getUser());

                    request.respond("Beskeden blev ændret");
                    break;
                case "DELETE_MESSAGE":
                    messageId = data.getLong("messageId");

                    deleteMessage(messageId, request.getUser());

                    request.respond("Beskeden blev slettet");
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
