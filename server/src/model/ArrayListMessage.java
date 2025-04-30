package model;

import java.util.Map;

public class ArrayListMessage implements Message {
    private long sentBy;
    private String body;
    private long dateTime;

    public ArrayListMessage(long sentBy, String body, long dateTime) {
        this.sentBy = sentBy;
        this.body = body;
        this.dateTime = dateTime;
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
    public Map<String, Object> getData() {
        return Map.of("sentBy", Long.toString(sentBy), "body", body, "dateTime", Long.toString(dateTime));
    }
}
