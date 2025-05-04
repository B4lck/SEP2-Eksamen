package viewModel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ChatMessage;
import model.ChatRoom;
import model.Model;
import util.ServerError;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

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

        model.getChatManager().addListener(this);

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
            roomNameProperty.set(model.getChatRoomManager().getChatRoom(viewState.getCurrentChatRoom()).getName());
            model.getChatManager().getMessages(chatroom, 10);
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            try {
                if (evt.getPropertyName().equals("MESSAGES")) {
                    messagesProperty.clear();
                    System.out.println(evt.getNewValue());
                    for (ChatMessage m : (ArrayList<ChatMessage>) evt.getNewValue()) {
                        if (m.getChatRoom() == viewState.getCurrentChatRoom()) {
                            messagesProperty.add(new ViewMessage() {{
                                sender = m.getSentBy() == 0 ? "System" : model.getProfileManager().getProfile(m.getSentBy()).getUsername();
                                body = m.getBody();
                                dateTime = LocalDateTime.ofEpochSecond(m.getDateTime() / 1000, (int) (m.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                            }});
                        }
                    }
                }
            } catch (ServerError e) {
                e.showAlert();
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
            model.getChatManager().getMessages(0, 10);
            greetingTextProperty.setValue("Hej " + model.getProfileManager().getCurrentUserProfile().getUsername() + "!");
            // Beskeder/Nuværende rum
            resetMessages();

            // Liste over rum
            this.roomsProperty.clear();
            for (ChatRoom chatRoom : model.getChatRoomManager().getChatRooms()) {
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

    public void sendMessage() {
        try {
            model.getChatManager().sendMessage(viewState.getCurrentChatRoom(), composeMessageProperty.getValue());
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
