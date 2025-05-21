package model;

import mediator.Broadcast;
import mediator.ServerRequest;
import utils.DataMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.*;
import java.util.*;

public class MessagesDBManager implements Messages {

    private final Model model;
    private final PropertyChangeSupport property = new PropertyChangeSupport(this);

    private final Map<Long, Message> messages = new HashMap<>();

    public MessagesDBManager(Model model) {
        this.model = model;
        model.addHandler(this);
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public Message sendMessage(long roomId, String messageBody, List<String> attachments, long senderUserId) {
        if (!model.getRooms().doesRoomExists(roomId))
            throw new IllegalStateException("Rummet findes ikke");

        if (senderUserId != 0 && model.getRooms().getRoom(roomId, senderUserId).isMuted(senderUserId))
            throw new IllegalStateException("Du snakker for meget brormand");

        if (messageBody == null)
            throw new IllegalArgumentException("Besked må ikke være null");

        if (messageBody.isBlank() && attachments.isEmpty())
            throw new IllegalArgumentException("Besked er tom");

        try (Connection connection = Database.getConnection()) {
            var currentTime = System.currentTimeMillis();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO message (body, sent_by_id, room_id, time) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, messageBody);
            statement.setLong(2, senderUserId);
            statement.setLong(3, roomId);
            statement.setLong(4, currentTime);
            statement.executeUpdate();
            ResultSet res = statement.getGeneratedKeys();

            if (res.next()) {
                long id = res.getLong(1);

                Message message = new DBMessage(id, senderUserId, messageBody, currentTime, roomId);

                messages.put(id, message);

                for (String attachment : attachments) {
                    message.addAttachment(attachment);
                }

                // Broadcast til klienter
                Room room = model.getRooms().getRoom(message.getRoomId());
                property.firePropertyChange("RECEIVE_MESSAGE", null, new Broadcast(new DataMap().with("message", message.getData()), room.getMembers()));

                return message;
            } else {
                throw new RuntimeException("Kunne ikke oprette besked i databasen, måske");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public List<Message> getMessages(long roomId, int amount, long userId) {
        if (amount <= 0) throw new IllegalArgumentException("Ikke nok beskeder");
        if (!model.getRooms().doesRoomExists(roomId))
            throw new IllegalStateException("Rummet findes ikke brormand");

        // Tjekker om brugeren har adgang til rummet
        // TODO: Lav en .hasAccessTo(chatroom, userId)
        model.getRooms().getRoom(roomId, userId);

        return getMessages(roomId, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public List<Message> getMessages(long roomId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Ikke nok beskeder");
        if (!model.getRooms().doesRoomExists(roomId))
            throw new IllegalStateException("Rummet findes ikke brormand");

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM message WHERE room_id = ? ORDER BY time DESC LIMIT ?;");
            statement.setLong(1, roomId);
            statement.setInt(2, amount);
            ResultSet res = statement.executeQuery();

            List<Message> msgs = new ArrayList<>();

            while (res.next()) {
                long id = res.getLong("id");

                if (!messages.containsKey(id)) {
                    messages.put(id, new DBMessage(
                            res.getLong("id"),
                            res.getLong("sent_by_id"),
                            res.getString("body"),
                            res.getLong("time"),
                            res.getLong("room_id")
                    ));
                }

                msgs.add(messages.get(id));
            }

            return msgs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public List<Message> getMessagesBefore(long messageId, int amount, long userId) {
        if (amount <= 0) throw new IllegalArgumentException("Det er for lidt beskeder brormand");

        Message beforeThis = getMessage(messageId, userId);

        if (beforeThis == null)
            throw new IllegalStateException("Beskeden findes ikke, eller du har ikke adgang til rummet.");

        // Hent beskeder i cache
        ArrayList<Message> msgs = new ArrayList<>(messages.values().stream()
                .filter(msg -> msg.getRoomId() == beforeThis.getRoomId()
                        && msg.getDateTime() <= beforeThis.getDateTime()
                        && msg != beforeThis)
                .sorted(Comparator.comparingLong(Message::getDateTime).reversed())
                .limit(10)
                .toList());

        // Blev nok beskeder hentet?
        if (msgs.size() == amount) return msgs;

        // Databasen skal kun hente fra før ældste besked fundet
        Message beforeThisOrOlder = beforeThis;
        if (!msgs.isEmpty()) beforeThisOrOlder = msgs.getLast();

        // Forsøg at hente fra databasen
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM message WHERE message.time < ? AND message.room_id = ? ORDER BY message.time DESC LIMIT ?;"
            );
            statement.setLong(1, beforeThisOrOlder.getDateTime());
            statement.setLong(2, beforeThisOrOlder.getRoomId());
            statement.setInt(3, amount - msgs.size());
            ResultSet res = statement.executeQuery();

            while (res.next()) {
                long id = res.getLong("id");

                if (!messages.containsKey(id)) {
                    messages.put(id, new DBMessage(
                            res.getLong("id"),
                            res.getLong("sent_by_id"),
                            res.getString("body"),
                            res.getLong("time"),
                            res.getLong("room_id")
                    ));
                }

                msgs.add(messages.get(id));
            }

            return msgs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public Message getMessage(long messageId, long userId) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM message WHERE id = ?");
            statement.setLong(1, messageId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                if (!messages.containsKey(messageId)) {
                    messages.put(messageId, new DBMessage(
                            res.getLong("id"),
                            res.getLong("sent_by_id"),
                            res.getString("body"),
                            res.getLong("time"),
                            res.getLong("room_id")
                    ));
                }

                model.getRooms().getRoom(res.getLong("room_id"), userId);

                return messages.get(messageId);
            } else {
                throw new IllegalStateException("Kunne ikke finde besked med id " + messageId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     * @see #sendMessage(long, String, List, long) Samme fejl kan blive kastet som fra sendMessage, udover dem med brugere.
     */
    @Override
    public void sendSystemMessage(long roomId, String message) {
        sendMessage(roomId, message, new ArrayList<>(), 0);
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     * En server-besked vil blive oprettet, som annoncer at en besked er blevet redigeret.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public void editMessage(long messageId, String messageBody, long userId) {
        // Hent beskeden
        var message = getMessage(messageId, userId);

        // Rediger beskeden
        message.editBody(messageBody, userId);

        // Broadcast til klienter
        Room chatroom = model.getRooms().getRoom(message.getRoomId());
        property.firePropertyChange("UPDATE_MESSAGE", null, new Broadcast(new DataMap().with("message", message.getData()), chatroom.getMembers()));

        // Send system besked
        sendSystemMessage(message.getRoomId(),
                model.getProfiles().getProfile(userId).map(Profile::getUsername).orElse("En bruger")
                        + " har ændret en besked.");
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     * En server-besked vil blive oprettet, som annoncer at en besked er blevet slettet.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public void deleteMessage(long messageId, long userId) {
        // Hent beskeden
        var message = getMessage(messageId, userId);

        // Gem bilag inden content bliver slettet
        List<String> attachments = message.getAttachments();

        // Slet selve beskeden
        message.deleteContent(userId);

        // Slet bilagene på serveren, når beskeden er blevet slettet
        UserFilesManager.getInstance().removeFiles(attachments);

        // Broadcast til klienter
        Room chatroom = model.getRooms().getRoom(message.getRoomId());
        property.firePropertyChange("UPDATE_MESSAGE", null, new Broadcast(new DataMap().with("message", message.getData()), chatroom.getMembers()));

        // Send system besked
        String username = model.getProfiles().getProfile(userId).map(Profile::getUsername).orElse("En bruger");
        sendSystemMessage(message.getRoomId(), username + " har slettet en besked.");
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public void addReaction(long messageId, String reaction, long userId) {
        Message message = getMessage(messageId, userId);

        message.addReaction(reaction, userId);

        // Broadcast til klienter
        Room chatroom = model.getRooms().getRoom(message.getRoomId());
        property.firePropertyChange("UPDATE_MESSAGE", null, new Broadcast(new DataMap().with("message", message.getData()), chatroom.getMembers()));

        // Send notifikation til senderen af beskeden
        property.firePropertyChange("NEW_REACTION", null, new Broadcast(new DataMap().with("message", message.getData()).with("reactedBy", userId).with("reaction", reaction), message.getSentBy()));
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public void removeReaction(long messageId, String reaction, long userId) {
        Message message = getMessage(messageId, userId);

        message.removeReaction(reaction, userId);

        // Broadcast til klienter
        Room chatroom = model.getRooms().getRoom(message.getRoomId());
        property.firePropertyChange("UPDATE_MESSAGE", null, new Broadcast(new DataMap().with("message", message.getData()), chatroom.getMembers()));
    }

    /**
     * {@inheritDoc}
     * En broadcast vil blive sendt ud til klienterne, hvis fuldført korrekt.
     *
     * @throws RuntimeException Hvis serveren støder på en SQL-fejl.
     */
    @Override
    public void setLatestReadMessage(long messageId, long userId) {
        Message message = getMessage(messageId, userId);
        long roomId = message.getRoomId();
        Room room = model.getRooms().getRoom(roomId, userId);
        RoomMember user = room.getMember(userId);
        long previousReadMessageId = user.getLatestReadMessage();

        if (previousReadMessageId == messageId) return;
        if (previousReadMessageId != 0 && message.getDateTime() <= getMessage(previousReadMessageId, userId).getDateTime())
            return;

        room.setLatestReadMessage(messageId, userId);

        property.firePropertyChange("READ_MESSAGE", null, new Broadcast(
                new DataMap()
                        .with("messageId", messageId)
                        .with("roomId", roomId)
                        .with("userId", userId),
                room.getMembers()
        ));
    }

    @Override
    public void handleRequest(ServerRequest request) {
        long roomId;
        var data = request.getData();
        int amount;
        long messageId;

        try {
            switch (request.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    roomId = data.getLong("roomId");
                    String messageBody = data.getString("body");

                    if (messageBody == null) messageBody = "";

                    List<String> attachments = new ArrayList<>();

                    // Hent attachments
                    while (!request.getAttachments().isEmpty()) {
                        var name = request.downloadNextAttachment();
                        attachments.add(name);
                    }

                    sendMessage(roomId, messageBody, attachments, request.getUser());

                    request.respond("Beskeden blev sendt");
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    roomId = data.getLong("roomId");
                    amount = data.getInt("amount");

                    request.respond(new DataMap().with("messages", toSendableData(getMessages(roomId, amount, request.getUser()))));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_BEFORE":
                    long before = request.getData().getLong("before");
                    amount = data.getInt("amount");

                    var messages = getMessagesBefore(before, amount, request.getUser());

                    if (messages.isEmpty()) {
                        request.respond(new DataMap()
                                .with("messages", new ArrayList<DataMap>())
                                .with("newestTime", 0));
                        return;
                    }

                    request.respond(new DataMap()
                            .with("messages", toSendableData(messages))
                            .with("newestTime", messages.getFirst().getDateTime()));
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
                case "ADD_REACTION":
                    addReaction(data.getLong("messageId"), data.getString("reaction"), request.getUser());
                    request.respond("Reaktionen blev tilføjet");
                    break;
                case "REMOVE_REACTION":
                    removeReaction(data.getLong("messageId"), data.getString("reaction"), request.getUser());
                    request.respond("Reaktionen blev fjernet");
                    break;
                case "READ_MESSAGE":
                    setLatestReadMessage(request.getData().getLong("messageId"), request.getUser());
                    request.respond("Success");
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
