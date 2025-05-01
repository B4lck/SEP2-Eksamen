package model;

import java.util.Map;

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

    public static ChatMessage fromData(Map<String, Object> message) {
        return new ChatMessage(
                Long.parseLong((String) message.get("sentBy")),
                (String) message.get("body"),
                Long.parseLong((String) message.get("dateTime")),
                Long.parseLong((String) message.get("id")),
                Long.parseLong((String) message.get("chatRoom"))
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