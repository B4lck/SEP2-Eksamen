package model;

import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBMessage implements Message {
    private long messageId;
    private String body;
    private long dateTime;
    private long sentBy;
    private long roomId;
    private List<String> attachments;
    private List<DBReaction> reactions;

    public DBMessage(long messageId, long sentBy, String body, long dateTime, long roomId) {
        this.messageId = messageId;
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.roomId = roomId;
    }

    @Override
    public long getMessageId() {
        return this.messageId;
    }

    @Override
    public long getSentBy() {
        return this.sentBy;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public long getDateTime() {
        return dateTime;
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("sentBy", getSentBy())
                .with("body", getBody())
                .with("dateTime", getDateTime())
                .with("messageId", getMessageId())
                .with("roomId", getRoomId())
                .with("attachments", getAttachments())
                .with("reactions", getReactions().stream().map(DBReaction::getData).toList());
    }

    @Override
    public long getRoomId() {
        return roomId;
    }

    @Override
    public List<String> getAttachments() {
        if (attachments != null) return attachments;

        // Hent fra databasen
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM attachment WHERE message_id = ?");
            statement.setLong(1, messageId);
            ResultSet res = statement.executeQuery();
            attachments = new ArrayList<>();
            while (res.next()) {
                attachments.add(res.getString("file_name"));
            }
            return attachments;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Reaction> getReactions() {
        if (reactions != null) return reactions;

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM reaction WHERE message_id = ?");
            statement.setLong(1, messageId);
            ResultSet res = statement.executeQuery();
            reactions = new ArrayList<>();
            while (res.next()) {
                reactions.add(new DBReaction(res.getLong("reacted_by"), res.getString("reaction")));
            }
            return reactions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editBody(String messageBody, long userId) {
        if (messageBody.isEmpty()) throw new IllegalArgumentException("Beskeden har intet indhold");
        if (userId != sentBy)
            throw new IllegalStateException("Du har ikke tilladelse til at redigere den her besked");
        // TODO: Man kunne godt tilføje noget lignende de her exceptions som triggers på serveren
        //       for lidt ekstra flair i rapporten

        try (Connection connection = Database.getConnection()) {
            body = messageBody + " (redigeret)";

            PreparedStatement statement = connection.prepareStatement("UPDATE message SET body = ? WHERE id = ?");
            statement.setString(1, body);
            statement.setLong(2, messageId);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteContent(long userId) {
        try (Connection connection = Database.getConnection()) {
            body = "[BESKEDEN ER BLEVET SLETTET]";
            attachments = new ArrayList<>();
            // Slet indholdet af beskeden
            PreparedStatement statement = connection.prepareStatement("UPDATE message SET body = ? WHERE id = ?");
            statement.setString(1, body);
            statement.setLong(2, messageId);
            statement.execute();
            // Slet beskedens bilag
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM attachment WHERE message_id = ?");
            statement2.setLong(1, messageId);
            statement2.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAttachment(String fileName) {
        try (Connection connection = Database.getConnection()) {
            getAttachments().add(fileName);

            PreparedStatement statement = connection.prepareStatement("INSERT INTO attachment (message_id, file_name) VALUES (?, ?)");
            statement.setLong(1, messageId);
            statement.setString(2, fileName);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAttachment(String fileName) {
        try (Connection connection = Database.getConnection()) {
            getAttachments().remove(fileName);

            PreparedStatement statement = connection.prepareStatement("DELETE FROM attachment WHERE message_id = ? AND file_name = ?");
            statement.setLong(1, messageId);
            statement.setString(2, fileName);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addReaction(String reaction, long userId) {
        if (reaction == null || reaction.isBlank()) throw new IllegalArgumentException("Reaction må ikke være null");

        try (Connection connection = Database.getConnection()) {
            reactions.add(new DBReaction(userId, reaction));

            PreparedStatement statement = connection.prepareStatement("INSERT INTO reaction (message_id, reacted_by, reaction) VALUES (?,?,?)");
            statement.setLong(1, messageId);
            statement.setLong(2, userId);
            statement.setString(3, reaction);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeReaction(String reaction, long userId) {
        if (reaction == null || reaction.isBlank()) throw new IllegalArgumentException("Reaction må ikke være null");

        try (Connection connection = Database.getConnection()) {
            reactions.removeIf(r -> r.getReactedBy() == userId && r.getReaction().equals(reaction));

            PreparedStatement statement = connection.prepareStatement("DELETE FROM reaction WHERE message_id = ? AND reacted_by = ? AND reaction = ?");
            statement.setLong(1, messageId);
            statement.setLong(2, userId);
            statement.setString(3, reaction);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DBMessage dbMessage = (DBMessage) o;
        return messageId == dbMessage.messageId;
    }
}