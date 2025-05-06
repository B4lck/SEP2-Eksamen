package model;

import utils.DataMap;

public class Message {
    private long sentBy;
    private String body;
    private long dateTime;
    private long messageId;
    private long chatRoom;

    public Message(long sentBy, String body, long dateTime, long messageId, long chatRoom) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.messageId = messageId;
        this.chatRoom = chatRoom;
    }

    public static Message fromData(DataMap message) {
        return new Message(
                message.getLong("sentBy"),
                message.getString("body"),
                message.getLong("dateTime"),
                message.getLong("id"),
                message.getLong("chatRoom")
        );
    }

    public long getSentBy() {
        return sentBy;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
}