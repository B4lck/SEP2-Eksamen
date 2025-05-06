package model;

import utils.DataMap;

public class ArrayListMessage implements Message {
    private long sentBy;
    private String body;
    private long dateTime;
    private long messageId;
    private long chatRoom;

    private static long nextMessageId = 0;

    public ArrayListMessage(long sentBy, String body, long dateTime, long chatRoom) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
        this.messageId = nextMessageId++;
        this.chatRoom = chatRoom;
    }

    @Override
    public long getMessageId() {
        return messageId;
    }

    @Override
    public long getSentBy() {
        return this.sentBy;
    }

    @Override
    public String getBody() {
        return this.body;
    }

    @Override
    public long getDateTime() {
        return this.dateTime;
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("sentBy", sentBy)
                .with("body", body)
                .with("dateTime", dateTime)
                .with("id", messageId)
                .with("chatRoom", chatRoom);
    }

    @Override
    public long getChatRoom() {
        return chatRoom;
    }

    @Override
    public void editBody(String messageBody, long byUserId) {
        if (byUserId != sentBy) throw new IllegalArgumentException("Du har ikke tilladelse til at slette den her besked");
        body = messageBody;
    }
}
