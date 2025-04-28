package model;

import java.util.Map;

public interface Message {
    long getSentBy();
    String getBody();
    long getDateTime();
    Map<String, Object> getData();
}
