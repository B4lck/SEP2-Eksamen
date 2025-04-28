package model;

import mediator.ClientMessage;
import mediator.ServerMessage;

import java.util.ArrayList;
import java.util.Map;

public class ChatRoomsArrayListManager implements ChatRooms, ClientMessageHandler {
    private ArrayList<Message> messages = new ArrayList<>();

    @Override
    public void sendMessage(long ChatRoomID, String messageBody, long senderID) {
        messages.add(new ArrayListMessage(senderID, messageBody, System.currentTimeMillis()));
    }

    @Override
    public ArrayList<Message> getMessages(long ChatRoomID, int amount) {
        return (ArrayList<Message>) messages.subList(0, amount);
    }

    @Override
    public ArrayList<Message> getMessagesSince(long ChatRoomID, long timestamp) {
        return (ArrayList<Message>) messages.subList(0, messages.indexOf(new ArrayListMessage(0, "", timestamp)));
    }

    @Override
    public void handleMessage(ServerMessage message) {
        long chatRoom;

        try {
            switch (message.getType()) {
                // Send besked
                case "SEND_MESSAGE":
                    chatRoom = (long) message.getData().get("chatroom");
                    String messageBody = (String) message.getData().get("body");

                    sendMessage(chatRoom, messageBody, message.getUser());
                    message.respond(new ClientMessage("SUCCESS", Map.of("status", "Message sent")));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES":
                    chatRoom = (long) message.getData().get("chatroom");
                    int amount = (int) message.getData().get("amount");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", getMessages(chatRoom, amount).toArray())));
                    break;
                // Hent antal beskeder
                case "RECEIVE_MESSAGES_SINCE":
                    chatRoom = (long) message.getData().get("chatroom");
                    long since = (int) message.getData().get("since");

                    message.respond(new ClientMessage("RECEIVE_MESSAGES", Map.of("messages", getMessagesSince(chatRoom, since).toArray())));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            message.respond(new ClientMessage(e.getMessage()));
        }
    }
}
