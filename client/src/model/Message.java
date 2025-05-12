package model;

import utils.DataMap;

import java.util.List;

public class Message {
    private long sentBy;
    private String body;
    private long dateTime;
    private long messageId;
    private long chatRoom;
    private List<String> attachments;

    public Message(long sentBy, String body, long dateTime, long messageId, long chatRoom, List<String> attachments) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.messageId = messageId;
        this.chatRoom = chatRoom;
        this.attachments = attachments;
    }

    public static Message fromData(DataMap message) {
        return new Message(
                message.getLong("sentBy"),
                message.getString("body"),
                message.getLong("dateTime"),
                message.getLong("id"),
                message.getLong("chatRoom"),
                message.getArray("attachments")
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

    public long getChatRoom() {
        return chatRoom;
    }

    public List<String> getAttachments() {
        return attachments;
    }
}