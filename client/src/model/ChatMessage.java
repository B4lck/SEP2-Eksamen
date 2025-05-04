package model;

import utils.DataMap;

public class ChatMessage {
    private long sentBy;
    private String body;
    private long dateTime;
    private long messageId;
    private long chatRoom;

    public ChatMessage(long sentBy, String body, long dateTime, long messageId, long chatRoom) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.messageId = messageId;
        this.chatRoom = chatRoom;
    }

    public static ChatMessage fromData(DataMap message) {
        return new ChatMessage(
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