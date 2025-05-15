package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import model.RoomUser;
import util.ServerError;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateEditChatRoomViewModel implements ViewModel {
    private StringProperty nameProperty;
    private ObservableList<ViewUser> membersProperty;
    private StringProperty titleProperty;
    private StringProperty errorProperty;
    private Model model;
    private ViewState viewState;

    private boolean edit = false;

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
        if (viewState.getCurrentChatRoom() == -1) {
            edit = false;
            titleProperty.set("Opret chat rum");
            membersProperty.clear();
            nameProperty.set("");
        } else {
            edit = true;
            titleProperty.set("Rediger chat rum");
            membersProperty.clear();
            try {
                var room = model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom());
                nameProperty.set(room.getName());
                for (RoomUser user : room.getUsers()) {
                    addUser(user.getUserId());
                }
            } catch (ServerError e) {
                e.showAlert();
            }
        }
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public ObservableList<ViewUser> getMembersProperty() {
        return membersProperty;
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }

    public StringProperty getErrorProperty() {
        return errorProperty;
    }

    public void addUser(long _userId) {
        try {
            Profile profile = model.getProfileManager().getProfile(_userId);
            ViewUser viewUser = new ViewUser(_userId, profile.getUsername());
            membersProperty.add(viewUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void removeUser(long userId) {
        membersProperty.removeIf(p -> p.getUserId() == userId);
    }

    public void muteUser(long userId) {
        try {
            model.getRoomManager().muteUser(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void unmuteUser(long userId) {
        try {
            model.getRoomManager().unmuteUser(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void promoteUser(long userId) {
        try {
            model.getRoomManager().promoteUser(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void demoteUser(long userId) {
        try {
            model.getRoomManager().demoteUser(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public boolean confirm() {
        if (nameProperty.isEmpty().get()) {
            errorProperty.setValue("Brormand der mangler et navn!!!");
            return false;
        }
        try {
            if (edit) {
                var room = model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom());

                // Fjern fjernede brugere og tilføj nye brugere, ved at compare imod de gamle brugere
                List<Long> previousProfiles = room.getUsers().stream().map(RoomUser::getUserId).toList();

                Set<Long> addedProfiles = new HashSet<>(
                        membersProperty.stream()
                                .map(ViewUser::getUserId)
                                .filter(p -> !previousProfiles.contains(p))
                                .toList());

                Set<Long> removedProfiles = new HashSet<>(
                        previousProfiles
                                .stream()
                                .filter(p -> membersProperty.stream().noneMatch(p2 -> p2.getUserId() == p))
                                .toList());

                for (Long profile : addedProfiles) {
                    if (profile != null) model.getRoomManager().addUser(room.getRoomId(), profile);
                }

                for (Long profile : removedProfiles) {
                    if (profile != null) model.getRoomManager().removeUser(room.getRoomId(), profile);
                }

                // Opdater gruppenavn
                if (!nameProperty.getValue().equals(room.getName()))
                    model.getRoomManager().setName(room.getRoomId(), nameProperty.get());
            } else {
                // Opret et nyt chat rum
                long room = model.getRoomManager().createRoom(nameProperty.getValue());
                for (ViewUser profile : membersProperty) {
                    model.getRoomManager().addUser(room, profile.getUserId());
                }
            }
        } catch (ServerError e) {
            e.showAlert();
            return false;
        }
        return true;
    }

    public boolean isEdit() {
        return edit;
    }
}
