package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

import java.util.HashSet;
import java.util.Set;

public class CreateEditChatRoomViewModel implements ViewModel {
    private StringProperty nameField;
    private ObservableList<ViewUser> profiles;
    private StringProperty titleText;
    private StringProperty errorText;
    private Model model;
    private ViewState viewState;

    private boolean isEdit = false;

    public CreateEditChatRoomViewModel(Model model, ViewState viewState) {
        nameField = new SimpleStringProperty();
        errorText = new SimpleStringProperty();
        titleText = new SimpleStringProperty();
        profiles = FXCollections.observableArrayList();
        this.model = model;
        this.viewState = viewState;
    }

    @Override
    public void reset() {
        if (viewState.getCurrentChatRoom() == -1) {
            isEdit = false;
            titleText.set("Opret chat rum");
            profiles.clear();
            nameField.set("");
        } else {
            isEdit = true;
            titleText.set("Rediger chat rum");
            profiles.clear();
            try {
                var room = model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom());
                nameField.set(room.getName());
                for (long userId : room.getUsers()) {
                    addUser(userId);
                }
            } catch (ServerError e) {
                e.showAlert();
            }
        }
    }

    public StringProperty getNameField() {
        return nameField;
    }

    public ObservableList<ViewUser> getProfiles() {
        return profiles;
    }

    public void addUser(long _userId) {
        try {
            Profile profile = model.getProfileManager().getProfile(_userId);
            ViewUser viewUser = new ViewUser() {{
                userId = _userId;
                username = profile.getUsername();
            }};
            profiles.add(viewUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void removeUser(long userId) {
        profiles.removeIf(p -> p.userId == userId);
    }

    public boolean confirm() {
        if (nameField.isEmpty().get()) {
            errorText.setValue("Brormand der mangler et navn!!!");
            return false;
        }
        try {
            if (isEdit) {
                var room = model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom());

                // Fjern fjernede brugere og tilføj nye brugere, ved at compare imod de gamle brugere
                var previousProfiles = room.getUsers();

                Set<Long> addedProfiles = new HashSet<>(
                        profiles.stream()
                                .map(p -> p.userId)
                                .filter(p -> !previousProfiles.contains(p)).toList());

                Set<Long> removedProfiles = new HashSet<>(
                        previousProfiles.stream()
                                .filter(p -> !profiles.stream().anyMatch(p2 -> p2.userId == p))
                                .toList());

                for (Long profile : addedProfiles) {
                    if (profile != null) model.getRoomManager().addUser(room.getRoomId(), profile);
                }

                for (Long profile : removedProfiles) {
                    if (profile != null) model.getRoomManager().removeUser(room.getRoomId(), profile);
                }

                // Opdater gruppenavn
                if (!nameField.getValue().equals(room.getName()))
                    model.getRoomManager().setName(room.getRoomId(), nameField.get());
            } else {
                // Opret et nyt chat rum
                long room = model.getRoomManager().createRoom(nameField.getValue());
                for (ViewUser profile : profiles) {
                    model.getRoomManager().addUser(room, profile.userId);
                }
            }
        } catch (ServerError e) {
            e.showAlert();
            return false;
        }
        return true;
    }

    public StringProperty getErrorTextProperty() {
        return errorText;
    }

    public StringProperty getTitleTextProperty() {
        return titleText;
    }
}
