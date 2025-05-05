package viewModel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Message;
import model.Room;
import model.Model;
import util.ServerError;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;

public class ChatRoomViewModel implements ViewModel, PropertyChangeListener {

    private ObservableList<ViewMessage> messagesProperty;
    private StringProperty composeMessageProperty;
    private ObservableList<ViewRoom> roomsProperty;
    private StringProperty roomNameProperty;
    private StringProperty greetingTextProperty;

    private Model model;
    private ViewState viewState;

    public ChatRoomViewModel(Model model, ViewState viewState) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = FXCollections.observableArrayList();
        this.greetingTextProperty = new SimpleStringProperty();

        this.roomNameProperty = new SimpleStringProperty();

        this.roomsProperty = FXCollections.observableArrayList();
        this.viewState = viewState;

        model.getMessagesManager().addListener(this);

        viewState.getCurrentChatRoomProperty().addListener((change) -> {
            resetMessages();
        });
    }

    public ObservableList<ViewRoom> getChatRoomsProperty() {
        return roomsProperty;
    }

    public void setChatRoom(long chatRoom) {
        viewState.setCurrentChatRoom(chatRoom);
    }

    public void resetMessages() {
        messagesProperty.clear();

        long chatroom = viewState.getCurrentChatRoom();
        if (chatroom == -1) {
            roomNameProperty.set("Vælg et rum!");
            return;
        }

        try {
            roomNameProperty.set(model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom()).getName());
            var initialMessages = model.getMessagesManager().getMessages(chatroom, 10);

            for (Message message : initialMessages) {
                addMessage(message);
            }
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    private void addMessage(Message message) {
        try {
            if (message.getChatRoom() == viewState.getCurrentChatRoom()) {
                messagesProperty.add(new ViewMessage() {{
                    sender = message.getSentBy() == 0 ? "System" : model.getProfileManager().getProfile(message.getSentBy()).getUsername();
                    body = message.getBody();
                    dateTime = LocalDateTime.ofEpochSecond(message.getDateTime() / 1000, (int) (message.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                    messageId = message.getMessageId();
                    isSystemMessage = message.getSentBy() == 0;
                }});
            }
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (evt.getPropertyName().equals("NEW_MESSAGE")) {
                addMessage((Message) evt.getNewValue());
            }
        });
    }

    public ObservableList<ViewMessage> getMessagesProperty() {
        return messagesProperty;
    }

    public StringProperty getComposeMessageProperty() {
        return composeMessageProperty;
    }

    public StringProperty getGreetingTextProperty() {
        return greetingTextProperty;
    }

    public StringProperty getRoomNameProperty() {
        return roomNameProperty;
    }

    @Override
    public void reset() {
        try {
            greetingTextProperty.setValue("Hej " + model.getProfileManager().getCurrentUserProfile().getUsername() + "!");

            // Beskeder
            resetMessages();

            // Liste over rum
            this.roomsProperty.clear();
            for (Room chatRoom : model.getRoomManager().getChatRooms()) {
                roomsProperty.add(new ViewRoom() {{
                    name = chatRoom.getName();
                    roomId = chatRoom.getRoomId();
                }});
            }
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    public void loadOlderMessages() {
        try {
            var messages = model.getMessagesManager().getMessagesBefore(viewState.getCurrentChatRoom(), messagesProperty.getFirst().messageId, 10);
            for (Message message : messages) {
                addMessage(message);
            }
            messagesProperty.sort(Comparator.comparing(o -> o.dateTime));
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    public void sendMessage() {
        try {
            model.getMessagesManager().sendMessage(viewState.getCurrentChatRoom(), composeMessageProperty.getValue());
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void editMessage(long messageId) {

    }

    public void deleteMessage(long messageId) {

    }
}
