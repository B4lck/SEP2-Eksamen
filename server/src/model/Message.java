package model;

public interface Message {
    long getSentBy();
    String getBody();
    long getDateTime();
}
