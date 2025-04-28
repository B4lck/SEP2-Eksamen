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
        return new Message(((Double) message.get("sentBy")).longValue(), (String) message.get("body"), ((Double) message.get("dateTime")).longValue());
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