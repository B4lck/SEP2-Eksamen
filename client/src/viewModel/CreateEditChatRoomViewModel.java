package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import model.Room;
import model.RoomMember;
import util.ServerError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateEditChatRoomViewModel implements ViewModel {
    private StringProperty nameProperty;
    private ObservableList<ViewRoomMember> membersProperty;
    private StringProperty titleProperty;
    private StringProperty errorProperty;
    private Model model;
    private ViewState viewState;

    private boolean edit = false;

    private String setColorTo = null;
    private String setFontTo = null;

    public CreateEditChatRoomViewModel(Model model, ViewState viewState) {
        nameProperty = new SimpleStringProperty();
        errorProperty = new SimpleStringProperty();
        titleProperty = new SimpleStringProperty();
        membersProperty = FXCollections.observableArrayList();
        this.model = model;
        this.viewState = viewState;
    }

    @Override
    public void reset() {
        setColorTo = null;
        setFontTo = null;
        if (viewState.getCurrentChatRoom() == -1) {
            edit = false;
            titleProperty.set("Opret chat rum");
            membersProperty.clear();
            nameProperty.set("");
            addMember(model.getProfileManager().getCurrentUserId(), "Creator");
        } else {
            edit = true;
            titleProperty.set("Rediger chat rum");
            membersProperty.clear();
            try {
                var room = model.getRoomManager().getRoom(viewState.getCurrentChatRoom());
                nameProperty.set(room.getName());
                for (RoomMember user : room.getMembers()) {
                    addMember(user.getUserId(), user.getState());
                }
            } catch (ServerError e) {
                e.showAlert();
            }
        }
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public ObservableList<ViewRoomMember> getMembersProperty() {
        return membersProperty;
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }

    public StringProperty getErrorProperty() {
        return errorProperty;
    }

    public void addMember(long userId, String defaultState) {
        if (membersProperty.stream().anyMatch(p -> p.getUserId() == userId)) return;
        try {
            Profile profile = model.getProfileManager().getProfile(userId);
            ViewRoomMember viewRoomUser = new ViewRoomMember(userId, profile.getUsername(), null, defaultState);
            membersProperty.add(viewRoomUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void removeMember(long userId) {
        membersProperty.removeIf(p -> p.getUserId() == userId && !p.getState().equals("Creator"));
    }

    public void muteMember(long userId) {
        ViewRoomMember member = membersProperty.stream().filter(m -> m.getUserId() == userId).findAny().orElseThrow();
        if (member.getState().equals("Regular")) member.setNewState("mute");
        else member.setNewState(null);
        triggerUpdateOnMember(userId);
    }

    public void unmuteMember(long userId) {
        ViewRoomMember member = membersProperty.stream().filter(m -> m.getUserId() == userId).findAny().orElseThrow();
        if (member.getState().equals("Muted")) member.setNewState("unmute");
        else member.setNewState(null);
        triggerUpdateOnMember(userId);
    }

    public void promoteMember(long userId) {
        ViewRoomMember member = membersProperty.stream().filter(m -> m.getUserId() == userId).findAny().orElseThrow();
        if (member.getState().equals("Regular")) member.setNewState("promote");
        else member.setNewState(null);
        triggerUpdateOnMember(userId);
    }

    public void demoteMember(long userId) {
        ViewRoomMember member = membersProperty.stream().filter(m -> m.getUserId() == userId).findAny().orElseThrow();
        if (member.getState().equals("Admin")) member.setNewState("demote");
        else member.setNewState(null);
        triggerUpdateOnMember(userId);
    }

    private void triggerUpdateOnMember(long userId) {
        ViewRoomMember member = membersProperty.stream().filter(m -> m.getUserId() == userId).findAny().orElseThrow();
        int index = membersProperty.indexOf(member);
        membersProperty.remove(index);
        membersProperty.add(index, member);
    }

    public boolean confirm() {
        if (nameProperty.isEmpty().get()) {
            errorProperty.setValue("Brormand der mangler et navn!!!");
            return false;
        }
        try {
            if (!edit) {
                // Opret et nyt chat rum
                long room = model.getRoomManager().createRoom(nameProperty.getValue());
                viewState.setCurrentChatRoom(room);
            }

            Room room = model.getRoomManager().getRoom(viewState.getCurrentChatRoom());

            // ANVEND ALLE ÆNDRINGER

            // 1.
            // Fjern fjernede brugere og tilføj nye brugere, ved at compare imod de gamle brugere
            List<Long> previousProfiles = room.getMembers().stream().map(RoomMember::getUserId).toList();

            Set<Long> addedProfiles = new HashSet<>(
                    membersProperty.stream()
                            .map(ViewRoomMember::getUserId)
                            .filter(p -> !previousProfiles.contains(p))
                            .toList());

            Set<Long> removedProfiles = new HashSet<>(
                    previousProfiles
                            .stream()
                            .filter(p -> membersProperty.stream().noneMatch(p2 -> p2.getUserId() == p))
                            .toList());

            for (Long profile : addedProfiles) {
                if (profile != null) model.getRoomManager().addMember(room.getRoomId(), profile);
            }

            for (Long profile : removedProfiles) {
                if (profile != null) model.getRoomManager().removeMember(room.getRoomId(), profile);
            }

            // 2.
            // Opdater bruger states
            for (ViewRoomMember changePlayerState : membersProperty) {
                if (changePlayerState.getNewState() == null) continue;
                switch (changePlayerState.getNewState()) {
                    case "mute" -> model.getRoomManager().muteMember(room.getRoomId(), changePlayerState.getUserId());
                    case "unmute" -> model.getRoomManager().unmuteMember(room.getRoomId(), changePlayerState.getUserId());
                    case "promote" -> model.getRoomManager().promoteMember(room.getRoomId(), changePlayerState.getUserId());
                    case "demote" -> model.getRoomManager().demoteMember(room.getRoomId(), changePlayerState.getUserId());
                }
            }

            // Opdater gruppenavn
            if (edit && !nameProperty.getValue().equals(room.getName()))
                model.getRoomManager().setName(room.getRoomId(), nameProperty.get());

            // Sæt chatrums farve
            if (setColorTo != null) model.getRoomManager().setColor(room.getRoomId(), setColorTo);

            // Sæt chatrums font
            if (setFontTo != null) model.getRoomManager().setFont(room.getRoomId(), setFontTo);
        } catch (ServerError e) {
            e.showAlert();
            return false;
        }
        return true;
    }

    public void setColor(String color) {
        setColorTo = color;
    }

    public String getRoomColor() {
        if (viewState.getCurrentChatRoom() == -1)
            return "#ffffff";
        try {
            return model.getRoomManager().getRoom(viewState.getCurrentChatRoom()).getColor();
        } catch (ServerError e) {
            e.showAlert();
            throw new RuntimeException(e);
        }
    }

    public void setFont(String newValue) {
        setFontTo = newValue;
    }

    public String getFont() {
        if (viewState.getCurrentChatRoom() == -1) return "Arial";

        try {
            return model.getRoomManager().getRoom(viewState.getCurrentChatRoom()).getFont();
        } catch (ServerError e) {
            e.showAlert();
            throw new RuntimeException(e);
        }
    }

    public boolean isEdit() {
        return edit;
    }
}
