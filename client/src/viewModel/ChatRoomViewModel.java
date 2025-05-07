package viewModel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.Attachment;
import model.Message;
import model.Room;
import model.Model;
import util.ServerError;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatRoomViewModel implements ViewModel, PropertyChangeListener {

    private ObservableList<ViewMessage> messagesProperty;
    private StringProperty composeMessageProperty;
    private ObservableList<ViewRoom> roomsProperty;
    private StringProperty roomNameProperty;
    private StringProperty greetingTextProperty;

    private Model model;
    private ViewState viewState;

    public ObservableList<Attachment> attachments;

    public ChatRoomViewModel(Model model, ViewState viewState) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = FXCollections.observableArrayList();
        this.greetingTextProperty = new SimpleStringProperty();

        this.roomNameProperty = new SimpleStringProperty();

        this.roomsProperty = FXCollections.observableArrayList();
        this.attachments = FXCollections.observableArrayList();
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
                List<File> files = new ArrayList<>();
                // Download attachments
                for (String attachmentName : message.getAttachments()) {
                    File file = model.getUserFileManager().getFile(attachmentName);
                    files.add(file);
                }

                // Tilføj besked
                messagesProperty.add(new ViewMessage() {{
                    sender = message.getSentBy() == 0 ? "System" : model.getProfileManager().getProfile(message.getSentBy()).getUsername();
                    body = message.getBody();
                    dateTime = LocalDateTime.ofEpochSecond(message.getDateTime() / 1000, (int) (message.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                    messageId = message.getMessageId();
                    isSystemMessage = message.getSentBy() == 0;
                    attachments = files;
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
            switch (evt.getPropertyName()) {
                case "NEW_MESSAGE":
                    addMessage((Message) evt.getNewValue());
                    break;
                case "UPDATE_VIEW_MODEL":
                    reset();
                    break;
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

    public ObservableList<Attachment> getAttachmentsProperty() {
        return attachments;
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
            model.getMessagesManager().sendMessage(viewState.getCurrentChatRoom(), composeMessageProperty.getValue(), attachments);
            attachments.clear();
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void editMessage(long messageId) {
        try {
            model.getMessagesManager().editMessage(messageId, composeMessageProperty.getValue());
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void deleteMessage(long messageId) {
        try {
            model.getMessagesManager().deleteMessage(messageId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
