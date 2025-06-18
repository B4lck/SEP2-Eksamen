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
import java.util.stream.Stream;

public class ChatRoomViewModel extends ViewModel implements PropertyChangeListener {

    private final StringProperty greetingTextProperty; // Velkommen tekst / Nuværende brugernavn
    private final ObjectProperty<ViewRoom> roomProperty; // Nuværende rum

    // Rum oversigt
    private final StringProperty searchFieldProperty;
    private SortingMethod sortingMethod = SortingMethod.ACTIVITY;
    private final ObservableList<ViewRoom> roomsProperty; // Alle brugerens rum

    // Chat
    private final StringProperty composeMessageProperty; // Nuværende indtastede besked i compose
    private final ObservableList<Attachment> attachmentsProperty; // De nuværende valgte attachments i compose
    private final ObservableList<ViewMessage> messagesProperty; // Indlæste beskeder i det nuværende rum
    private final ObservableList<ViewRoomMember> roomUsersProperty; // Brugere i det nuværende rum
    private final ObjectProperty<ViewRoomMember> currentRoomMemberProperty; // Den nuværende bruger i det nuværende rum

    private final ViewState viewState;

    public ChatRoomViewModel(Model model, ViewState viewState) {
        super(model);

        this.greetingTextProperty = new SimpleStringProperty();
        this.roomProperty = new SimpleObjectProperty<>();
        this.searchFieldProperty = new SimpleStringProperty();

        this.composeMessageProperty = new SimpleStringProperty();
        this.attachmentsProperty = FXCollections.observableArrayList();

        this.roomsProperty = FXCollections.observableArrayList();
        this.messagesProperty = FXCollections.observableArrayList();
        this.roomUsersProperty = FXCollections.observableArrayList();
        this.currentRoomMemberProperty = new SimpleObjectProperty<>();

        this.viewState = viewState;

        // Tilføj listeners til modellen
        model.getMessagesManager().addListener(this); // For nye beskeder
        model.getRoomManager().addListener(this); // For ændringer på rummet og read-receipts

        // Reset rummet når et nyt rum vælges
        viewState.getCurrentChatRoomProperty().addListener((_) -> resetRoom());

        // Marker den nyeste besked som læst, når vinduet bliver fokuseret
        ViewHandler.focusedProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                try {
                    model.getMessagesManager().readMessage(messagesProperty.getLast().messageId);
                } catch (ServerError e) {
                    // Tom med vanilje
                }
            }
        });
    }

    /**
     * Tekst-stykke der viser hvem der er logget ind
     */
    public StringProperty getGreetingTextProperty() {
        return greetingTextProperty;
    }

    /**
     * Indeholder navnet på det nuværende valgte rum
     */
    public ObjectProperty<ViewRoom> getRoomProperty() {
        return roomProperty;
    }

    /**
     * Indeholder teksten på søgefeltet i chatrum
     */
    public StringProperty getSearchFieldProperty() {
        return searchFieldProperty;
    }

    /**
     * Indholdet bliver sendt når sendMessage eller editMessage kaldes.
     */
    public StringProperty getComposeMessageProperty() {
        return composeMessageProperty;
    }

    /**
     * Indeholder de nuværende valgte attachments, som bliver sendt når sendMessage kaldes.
     */
    public ObservableList<Attachment> getAttachmentsProperty() {
        return attachmentsProperty;
    }

    /**
     * Liste over alle de rum brugeren er medlem af
     */
    public ObservableList<ViewRoom> getRoomsProperty() {
        return roomsProperty;
    }

    /**
     * Liste over alle beskeder i det nuværende rum
     */
    public ObservableList<ViewMessage> getMessagesProperty() {
        return messagesProperty;
    }

    /**
     * Liste over alle medlemmer af det nuværende rum
     */
    public ObservableList<ViewRoomMember> getRoomMembersProperty() {
        return roomUsersProperty;
    }

    /**
     * Medlemmet som er logget ind
     */
    public ObjectProperty<ViewRoomMember> getCurrentRoomMemberProperty() {
        return currentRoomMemberProperty;
    }

    /**
     * Skift det nuværende chatrum i view staten
     */
    public void setRoom(long roomId) {
        viewState.setCurrentChatRoom(roomId);
        // Der er en listener på currentChatRoom, som resetter rummet :)
    }

    /**
     * Nulstiller det nuværende rum, bruges kun efter rum-skifte
     */
    public void resetRoom() {
        // Clear
        roomUsersProperty.clear();
        messagesProperty.clear();

        long roomId = viewState.getCurrentChatRoom();

        // Stop tidligt hvis brugeren ikke er i et rum
        if (roomId == -1) {
            roomProperty.set(null);
            return;
        }

        try {
            Room room = model.getRoomManager().getRoom(roomId);
            // # Hent medlemmer
            // ID'et på den nuværende bruger
            long myId = model.getProfileManager().getCurrentUserId();
            // Tilføj brugere
            for (RoomMember user : room.getMembers()) {
                // Opret ViewRoomMember, som view'et kan bruge til display
                Profile profile = model.getProfileManager().getProfile(user.getUserId());
                var member = new ViewRoomMember(
                        user.getUserId(),
                        profile.getUsername(),
                        user.getNickname(),
                        user.getState(),
                        user.getLatestReadMessage(),
                        profile.getLastActive(),
                        false
                );

                // Dette er RoomMember objektet til den nuværende bruger
                if (member.getUserId() == myId) {
                    currentRoomMemberProperty.set(member);
                }

                // Tilføj ViewRoomMember til property
                roomUsersProperty.add(member);
            }

            // # Opdater chatrummets oplysninger
            roomProperty.set(new ViewRoom(room.getName(), roomId, room.getLatestActivityTime(), room.getColor(), room.getFont(), false));

            // # Hent beskeder
            List<Message> initialMessages = model.getMessagesManager().getMessages(roomId, 10);

            for (Message message : initialMessages) {
                addMessage(message);
            }

            // Marker den nyeste besked i rummet som læst
            if (!initialMessages.isEmpty())
                model.getMessagesManager().readMessage(initialMessages.getLast().getMessageId());
        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    /**
     * Tilføj en ny besked, eller opdater beskeden hvis den allerede findes
     *
     * @param message Tilføj en besked
     */
    private void addMessage(Message message) {
        if (message.getRoomId() == viewState.getCurrentChatRoom()) {
            List<File> files = new ArrayList<>();
            // Download attachments
            for (String attachmentName : message.getAttachments()) {
                File file = model.getUserFileManager().getFile(attachmentName);
                files.add(file);
            }

            // Fjern hvis det er en redigering
            messagesProperty.removeIf(m -> m.messageId == message.getMessageId());

            long myId = model.getProfileManager().getCurrentUserId();

            // "Stak" reactions
            Map<String, ViewReaction> messageReactions = new HashMap<>();
            for (Reaction reaction : message.getReactions()) {
                if (messageReactions.containsKey(reaction.getReaction())) {
                    messageReactions.get(reaction.getReaction()).addReactedBy(
                            reaction.getReactedBy(),
                            reaction.getReactedBy() == myId
                    );
                } else {
                    messageReactions.put(reaction.getReaction(), new ViewReaction(
                            reaction.getReaction(),
                            reaction.getReactedBy(),
                            reaction.getReactedBy() == myId
                    ));
                }
            }

            boolean isBlocked = model.getProfileManager().isBlocked(message.getSentBy());

            // Tilføj besked
            messagesProperty.add(new ViewMessage() {{
                sender = isBlocked ? "<<blokeret bruger>>" : roomUsersProperty.stream()
                        .filter(u -> u.getUserId() == message.getSentBy()).findAny()
                        .map(ViewRoomMember::getDisplayName).orElse("System");
                body = isBlocked ? "<<besked fra en blokeret bruger>>" : message.getBody();
                dateTime = LocalDateTime.ofEpochSecond(message.getDateTime() / 1000, (int) (message.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                messageId = message.getMessageId();
                isSystemMessage = message.getSentBy() == 0;
                isMyMessage = message.getSentBy() == myId;
                attachments = isBlocked ? List.of() : files;
                reactions = isBlocked ? List.of() : messageReactions.values().stream().toList();
            }});

            messagesProperty.sort(Comparator.comparing(o -> o.dateTime));
            resetRooms();
        }
    }

    /**
     * Lyt efter opdateringer i modellen
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            try {
                switch (evt.getPropertyName()) {
                    // NEW_MESSAGE og UPDATE_MESSAGE events, gør begge det samme, da addMessage selv opdager hvis beskeden allerede findes
                    case "NEW_MESSAGE":
                    case "UPDATE_MESSAGE":
                        Message message = (Message) evt.getNewValue();
                        addMessage(message);
                        if (ViewHandler.focusedProperty.get())
                            model.getMessagesManager().readMessage(message.getMessageId());
                        break;
                    // Opdater read receipts
                    case "READ_UPDATE":
                        if ((long) evt.getOldValue() != viewState.getCurrentChatRoom()) return;
                        RoomMember roomUser = (RoomMember) evt.getNewValue();
                        roomUsersProperty.removeIf(viewUser -> viewUser.getUserId() == roomUser.getUserId());
                        Profile userProfile = model.getProfileManager().getProfile(roomUser.getUserId());
                        roomUsersProperty.add(new ViewRoomMember(
                                roomUser.getUserId(),
                                userProfile.getUsername(),
                                roomUser.getNickname(),
                                roomUser.getState(),
                                roomUser.getLatestReadMessage(),
                                userProfile.getLastActive(),
                                false
                        ));
                        resetRooms();
                        break;
                    // Opdater rum
                    case "ROOM_CHANGED":
                        resetRoom();
                        resetRooms();
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

            // Opdater selve rummet, beskeder osv.
            resetRoom();
            resetRooms();
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    /**
     * Hent ældre beskeder i det nuværende rum
     */
    public void loadOlderMessages() {
        if (viewState.getCurrentChatRoom() == -1) return;

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

    /**
     * Sender en besked med teksten i composeMessageProperty, og de attachments der er i attachmentsProperty
     */
    public void sendMessage() {
        try {
            model.getMessagesManager()
                    .sendMessage(viewState.getCurrentChatRoom(), composeMessageProperty.getValue(), attachmentsProperty);
            attachmentsProperty.clear();
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    /**
     * Redigere en besked med den tekst som er i composeMessageProperty
     *
     * @param messageId - ID'et på den besked som skal redigeres
     */
    public void editMessage(long messageId) {
        try {
            model.getMessagesManager().editMessage(messageId, composeMessageProperty.getValue());
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    /**
     * Sletter en besked
     *
     * @param messageId - ID'et på den besked som skal slettes
     */
    public void deleteMessage(long messageId) {
        try {
            model.getMessagesManager().deleteMessage(messageId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    /**
     * Tilføjer en reaktion på en besked
     *
     * @param messageId - ID'et på den besked som reageres på
     * @param reaction  - Streng med den emoji der reageres med
     */
    public void addReaction(long messageId, String reaction) {
        try {
            model.getMessagesManager().addReaction(messageId, reaction);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    /**
     * Sletter en reaktion på en besked
     *
     * @param messageId - ID'et på den besked som ikke længere skal reageres på
     * @param reaction  - Streng med den emoji der ikke længere skal reageres med
     */
    public void removeReaction(long messageId, String reaction) {
        try {
            model.getMessagesManager().removeReaction(messageId, reaction);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void resetRooms() {
        try {
            this.roomsProperty.clear();
            String query = searchFieldProperty.get() == null ? "" : searchFieldProperty.get();

            Stream<ViewRoom> tempRooms = model.getRoomManager().getMyRooms().stream()
                    .filter(r -> r.getName().toLowerCase().contains(query.toLowerCase()))
                    .map(r -> new ViewRoom(
                            r.getName(),
                            r.getRoomId(),
                            r.getLatestActivityTime(),
                            r.getColor(),
                            r.getFont(),
                            r.getLatestMessageId() != r.getUser(model.getProfileManager().getCurrentUserId()).map(RoomMember::getLatestReadMessage).orElse(-1L)));

            tempRooms = switch (sortingMethod) {
                case ALPHABETICALLY -> tempRooms.sorted((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                case ACTIVITY -> tempRooms.sorted(Comparator.comparingLong(ViewRoom::getLatestActivity).reversed());
            };

            roomsProperty.addAll(tempRooms.toList());
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void setSortingMethod(SortingMethod sortingMethod) {
        this.sortingMethod = sortingMethod;
        resetRooms();
    }

    public void logout() {
        try {
            model.getProfileManager().logout();
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
