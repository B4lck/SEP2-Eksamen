package model;

import utils.DataMap;

import java.util.List;

public class Message {
    private final long messageId;
    private final long roomId;
    private final String body;
    private final long sentBy;
    private final long dateTime;
    private final List<String> attachments;
    private final List<Reaction> reactions;

    public Message(long messageId, long roomId, String body, long sentBy, long dateTime, List<String> attachments, List<Reaction> reactions) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.body = body;
        this.sentBy = sentBy;
        this.dateTime = dateTime;
        this.attachments = attachments;
        this.reactions = reactions;
    }

    public static Message fromData(DataMap message) {
        return new Message(
                message.getLong("messageId"),
                message.getLong("roomId"),
                message.getString("body"),
                message.getLong("sentBy"),
                message.getLong("dateTime"),
                message.getArray("attachments"),
                message.getMapArray("reactions").stream().map(Reaction::fromData).toList()
        );
    }

    public long getSentBy() {
        return sentBy;
    }

    public String getBody() {
        return body;
    }

    public long getDateTime() {
        return dateTime;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getRoomId() {
        return roomId;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public List<Reaction> getReactions() {
        return reactions;
    }
}