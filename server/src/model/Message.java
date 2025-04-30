package model;

import java.util.Map;

public interface Message {
    long getMessageId();
    long getSentBy();
    String getBody();
    long getDateTime();
    Map<String, Object> getData();
}
