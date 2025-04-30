package model;

import java.util.Map;

public class Message {
    private long sentBy;
    private String body;
    private long dateTime;

    public Message(long sentBy, String body, long dateTime) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
    }

    public static Message fromData(Map<String, Object> message) {
        return new Message(Long.parseLong((String) message.get("sentBy")), (String) message.get("body"), Long.parseLong((String) message.get("dateTime")));
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
}