package model;

import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DBMessage implements Message {
    private long id;
    private long sentBy;
    private String body;
    private long dateTime;
    private long chatRoom;

    public DBMessage(long id, long sentBy, String body, long dateTime, long chatRoom) {
        this.id = id;
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.chatRoom = chatRoom;
    }

    @Override
    public long getMessageId() {
        return this.id;
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
                .with("id", getMessageId())
                .with("chatRoom", getChatRoom())
                .with("attachments", getAttachments());
    }

    @Override
    public long getChatRoom() {
        return chatRoom;
    }

    @Override
    public List<String> getAttachments() {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM attachment WHERE message_id = ?");
            statement.setLong(1, id);
            ResultSet res = statement.executeQuery();
            List<String> attachments = new ArrayList<>();
            while (res.next()) {
                attachments.add(res.getString("file_name"));
            }
            return attachments;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editBody(String messageBody, long byUserId) {
        if (messageBody.isEmpty()) throw new IllegalArgumentException("Beskeden har intet indhold");
        if (byUserId != sentBy) throw new IllegalStateException("Du har ikke tilladelse til at redigere den her besked");
        // TODO: Man kunne godt tilføje noget ligende de her exceptions som triggers på serveren
        //       for lidt ekstra flair i rapporten

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE message SET body = ? WHERE id = ?");
            statement.setString(1, messageBody + " (redigeret)");
            statement.setLong(2, id);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteContent(long byUserId) {
        try (Connection connection = Database.getConnection()) {
            // Slet indholdet af beskeden
            PreparedStatement statement = connection.prepareStatement("UPDATE message SET body = ? WHERE id = ?");
            statement.setString(1, "[BESKEDEN ER BLEVET SLETTET]");
            statement.setLong(2, id);
            statement.execute();
            // Slet beskedens bilag
            PreparedStatement statement2 = connection.prepareStatement("DELETE FROM attachment WHERE message_id = ?");
            statement2.setLong(1, id);
            statement2.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAttachment(String fileName) {
        try (Connection connection = Database.getConnection()) {
            // Tilføj bilag til beskeden
            PreparedStatement statement = connection.prepareStatement("INSERT INTO attachment (message_id, file_name) VALUES (?, ?)");
            statement.setLong(1, id);
            statement.setString(2, fileName);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAttachment(String fileName) {
        try (Connection connection = Database.getConnection()) {
            // Tilføj bilag til beskeden
            PreparedStatement statement = connection.prepareStatement("DELETE FROM attachment WHERE message_id = ? AND file_name = ?");
            statement.setLong(1, id);
            statement.setString(2, fileName);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DBMessage dbMessage = (DBMessage) o;
        return id == dbMessage.id;
    }
}