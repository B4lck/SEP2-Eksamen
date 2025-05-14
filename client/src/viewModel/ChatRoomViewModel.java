package viewModel;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;
import util.Attachment;
import util.ServerError;
import view.ViewHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class ChatRoomViewModel implements ViewModel, PropertyChangeListener {

    private ObservableList<ViewMessage> messagesProperty;
    private StringProperty composeMessageProperty;
    private ObservableList<ViewRoom> roomsProperty;
    private StringProperty roomNameProperty;
    private StringProperty greetingTextProperty;
    private ObservableList<ViewRoomUser> roomUsersProperty;
    private ObjectProperty<ViewRoomUser> currentRoomUserProperty;

    private Model model;
    private ViewState viewState;

    private ObservableList<Attachment> attachments;

    public ChatRoomViewModel(Model model, ViewState viewState) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = FXCollections.observableArrayList();
        this.greetingTextProperty = new SimpleStringProperty();
        this.roomUsersProperty = FXCollections.observableArrayList();
        this.currentRoomUserProperty = new SimpleObjectProperty<>();

        this.roomNameProperty = new SimpleStringProperty();

        this.roomsProperty = FXCollections.observableArrayList();
        this.attachments = FXCollections.observableArrayList();
        this.viewState = viewState;

        model.getMessagesManager().addListener(this);
        model.getRoomManager().addListener(this);

        viewState.getCurrentChatRoomProperty().addListener((change) -> resetRoom());

        ViewHandler.focusedProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                try {
                    model.getMessagesManager().readMessage(messagesProperty.getLast().messageId);
                } catch (ServerError e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public ObservableList<ViewRoom> getChatRoomsProperty() {
        return roomsProperty;
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

    public ObservableList<ViewRoomUser> getRoomUsersProperty() {
        return roomUsersProperty;
    }

    public ObjectProperty<ViewRoomUser> getCurrentRoomUserProperty() {
        return currentRoomUserProperty;
    }

    public ObservableList<Attachment> getAttachmentsProperty() {
        return attachments;
    }

    public void setChatRoom(long chatRoom) {
        viewState.setCurrentChatRoom(chatRoom);
    }

    public void resetRoom() {
        System.out.println("====");
        roomUsersProperty.clear();
        messagesProperty.clear();

        if (viewState.getCurrentChatRoom() == -1) return;

        try {
            // Medlemmer
            long myId = model.getProfileManager().getCurrentUserUUID();
            // Tilføj brugere
            for (RoomUser user : model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom()).getUsers()) {
                var vru = new ViewRoomUser(
                        user.getUserId(),
                        model.getProfileManager().getProfile(user.getUserId()).getUsername(),
                        "", // TODO: Nickname
                        user.getState(),
                        user.getLatestReadMessage()
                );

                if (vru.getUserId() == myId) {
                    currentRoomUserProperty.set(vru);
                }

                roomUsersProperty.add(vru);
            }

            // Beskeder
            long chatroom = viewState.getCurrentChatRoom();
            if (chatroom == -1) {
                roomNameProperty.set("Vælg et rum!");
                return;
            }

            roomNameProperty.set(model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom()).getName());
            var initialMessages = model.getMessagesManager().getMessages(chatroom, 10);

            for (Message message : initialMessages) {
                addMessage(message);
            }

            model.getMessagesManager().readMessage(initialMessages.getLast().getMessageId());
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

                // Fjern hvis det er en redigering
                messagesProperty.removeIf(m -> m.messageId == message.getMessageId());

                Map<String, ViewReaction> messageReactions = new HashMap<>();

                // Udregn reactions
                for (Reaction reaction : message.getReactions()) {
                    Profile reactedBy = model.getProfileManager().getProfile(reaction.getReactedBy());
                    if (messageReactions.containsKey(reaction.getReaction())) {
                        messageReactions.get(reaction.getReaction()).reactedByUsers.add(new ViewUser() {{
                            username = reactedBy.getUsername();
                            userId = reaction.getReactedBy();
                        }});
                        if (reaction.getReactedBy() == model.getProfileManager().getCurrentUserUUID())
                            messageReactions.get(reaction.getReaction()).isMyReaction = true;
                    } else {
                        messageReactions.put(reaction.getReaction(), new ViewReaction(
                                reaction.getReaction(),
                                new ViewUser() {{
                                    username = reactedBy.getUsername();
                                    userId = reaction.getReactedBy();
                                }},
                                reaction.getReactedBy() == model.getProfileManager().getCurrentUserUUID()
                        ));
                    }
                }

                // Tilføj besked
                messagesProperty.add(new ViewMessage() {{
                    sender = message.getSentBy() == 0 ? "System" : (model.getProfileManager().getProfile(message.getSentBy()).getUsername());
                    body = message.getBody();
                    dateTime = LocalDateTime.ofEpochSecond(message.getDateTime() / 1000, (int) (message.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                    messageId = message.getMessageId();
                    isSystemMessage = message.getSentBy() == 0;
                    isMyMessage = message.getSentBy() == model.getProfileManager().getCurrentUserUUID();
                    attachments = files;
                    reactions = messageReactions.values().stream().toList();
                }});

                messagesProperty.sort(Comparator.comparing(o -> o.dateTime));
            }
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            try {
                switch (evt.getPropertyName()) {
                    case "NEW_MESSAGE":
                    case "UPDATE_MESSAGE":
                        Message message = (Message) evt.getNewValue();
                        addMessage(message);
                        if (ViewHandler.focusedProperty.get())
                            model.getMessagesManager().readMessage(message.getMessageId());
                        break;
                    case "READ_UPDATE":
                        if ((long) evt.getOldValue() != viewState.getCurrentChatRoom()) return;

                        RoomUser roomUser = (RoomUser) evt.getNewValue();
                        roomUsersProperty.removeIf(viewUser -> viewUser.getUserId() == roomUser.getUserId());
                        roomUsersProperty.add(new ViewRoomUser(
                                roomUser.getUserId(),
                                model.getProfileManager().getProfile(roomUser.getUserId()).getUsername(),
                                "", // TODO: Nickname
                                roomUser.getState(),
                                roomUser.getLatestReadMessage()
                        ));
                        break;
                }
            } catch (ServerError e) {
                e.printStackTrace();
                // Ik spam med fejl, da det er broadcast
            }
        });
    }

    @Override
    public void reset() {
        try {
            greetingTextProperty.setValue("Hej " + model.getProfileManager().getCurrentUserProfile().getUsername() + "!");

            resetRoom();

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

    public void addReaction(long messageId, String reaction) {
        try {
            model.getMessagesManager().addReaction(messageId, reaction);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void removeReaction(long messageId, String reaction) {
        try {
            model.getMessagesManager().removeReaction(messageId, reaction);
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
